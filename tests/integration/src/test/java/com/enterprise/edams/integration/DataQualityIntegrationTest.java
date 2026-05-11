package com.enterprise.edams.integration;

import io.restassured.RestAssured;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataQualityIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DataQualityIntegrationTest.class);

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("edams_test")
            .withUsername("test")
            .withPassword("test123")
            .withExposedPorts(3306);

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private WebApplicationContext context;

    private static String testAssetId;
    private static String testCheckId;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);
        RestAssured.port = 0;
    }

    @Test
    @Order(1)
    void shouldCreateQualityRule() {
        String requestBody = """
            {
                "name": "Not Null Check Rule",
                "type": "NULL_CHECK",
                "description": "检查字段是否为空",
                "enabled": true,
                "severity": "HIGH",
                "dimension": "COMPLETENESS"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/rules")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Not Null Check Rule"))
            .body("type", equalTo("NULL_CHECK"))
            .body("enabled", equalTo(true));

        log.info("Quality rule created successfully");
    }

    @Test
    @Order(2)
    void shouldListQualityRules() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/quality/rules")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThanOrEqualTo(1)))
            .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(3)
    void shouldTriggerQualityCheck() {
        String requestBody = """
            {
                "assetId": "test-asset-001",
                "rules": [
                    {"type": "NULL_CHECK", "enabled": true},
                    {"type": "UNIQUE_CHECK", "enabled": true},
                    {"type": "FORMAT_CHECK", "enabled": true, "pattern": "^[0-9]+$"}
                ]
            }
            """;

        Response response = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/check")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.checkId", notNullValue())
            .body("data.totalRules", equalTo(3))
            .extract().response();

        testCheckId = response.jsonPath().getString("data.checkId");
        log.info("Quality check triggered with ID: {}", testCheckId);
    }

    @Test
    @Order(4)
    void shouldGetCheckResult() {
        String checkId = "check-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/quality/check/" + checkId)
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.checkId", equalTo(checkId))
            .body("data.totalRules", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(5)
    void shouldGetCheckProgress() {
        String checkId = "check-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/quality/check/" + checkId + "/progress")
            .then()
            .statusCode(200)
            .body("progress", notNullValue())
            .body("status", anyOf(equalTo("RUNNING"), equalTo("COMPLETED"), equalTo("FAILED")));
    }

    @Test
    @Order(6)
    void shouldListQualityIssues() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .param("page", 0)
            .param("size", 20)
            .when()
            .get("/api/v1/quality/issues")
            .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    @Order(7)
    void shouldGetIssueStatistics() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/quality/issues/statistics")
            .then()
            .statusCode(200)
            .body("totalIssues", greaterThanOrEqualTo(0))
            .body("openIssues", greaterThanOrEqualTo(0))
            .body("resolvedIssues", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(8)
    void shouldResolveQualityIssue() {
        String issueId = "issue-001";

        String requestBody = """
            {
                "resolution": "FIXED",
                "comment": "问题已修复"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/issues/" + issueId + "/resolve")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Test
    @Order(9)
    void shouldBatchTriggerQualityCheck() {
        String requestBody = """
            {
                "assetIds": ["test-asset-001", "test-asset-002", "test-asset-003"],
                "rules": [
                    {"type": "NULL_CHECK", "enabled": true},
                    {"type": "UNIQUE_CHECK", "enabled": true}
                ]
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/check/batch")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.totalCount", equalTo(3))
            .body("data.completedCount", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(10)
    void shouldUpdateQualityRule() {
        String ruleId = "rule-001";

        String requestBody = """
            {
                "name": "Updated Rule Name",
                "description": "Updated description",
                "enabled": false
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .put("/api/v1/quality/rules/" + ruleId)
            .then()
            .statusCode(200)
            .body("id", equalTo(ruleId))
            .body("name", equalTo("Updated Rule Name"))
            .body("enabled", equalTo(false));
    }

    @Test
    @Order(11)
    void shouldDeleteQualityRule() {
        String ruleId = "rule-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .delete("/api/v1/quality/rules/" + ruleId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(12)
    void shouldIgnoreQualityIssue() {
        String issueId = "issue-001";

        String requestBody = """
            {
                "reason": "此问题为误报"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/issues/" + issueId + "/ignore")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Test
    @Order(13)
    void shouldGetRuleTemplates() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/quality/rules/templates")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(14)
    void shouldTransferIssue() {
        String issueId = "issue-001";

        String requestBody = """
            {
                "assignee": "new_assignee",
                "reason": "负责人变更"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/issues/" + issueId + "/transfer")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Test
    @Order(15)
    void shouldCloseQualityIssue() {
        String issueId = "issue-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .post("/api/v1/quality/issues/" + issueId + "/close")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Test
    @Order(16)
    void shouldValidateRuleType() {
        String requestBody = """
            {
                "name": "Invalid Rule",
                "type": "INVALID_TYPE"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/rules")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(17)
    void shouldRequireAuthForQualityCheck() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/v1/quality/check")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(18)
    @Disabled("跨服务集成测试，需要quality-service运行")
    void shouldTriggerCrossServiceIntegration() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("assetId", "test-asset-001");
        requestBody.put("triggerLineageUpdate", true);

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/quality/check")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("data.lineageUpdated", equalTo(true));
    }

    @Test
    @Order(19)
    void shouldCheckQualityScoreCalculation() {
        Response response = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/quality/check/check-001")
            .then()
            .statusCode(200)
            .body("data.qualityScore", notNullValue())
            .extract().response();

        Double qualityScore = response.jsonPath().getDouble("data.qualityScore");
        assertThat(qualityScore).isBetween(0.0, 100.0);
    }
}
