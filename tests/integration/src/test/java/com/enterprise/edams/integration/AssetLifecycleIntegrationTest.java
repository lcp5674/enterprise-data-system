package com.enterprise.edams.integration;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
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

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetLifecycleIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AssetLifecycleIntegrationTest.class);

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

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @Test
    @Order(1)
    void shouldCreateAssetInDraftStatus() {
        String requestBody = """
            {
                "name": "Test Lifecycle Asset",
                "assetType": "TABLE",
                "description": "Lifecycle test asset",
                "owner": "admin",
                "sensitivityLevel": "INTERNAL"
            }
            """;

        testAssetId = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/assets")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("status", equalTo("DRAFT"))
            .extract().path("id");

        log.info("Created asset with ID: {} in DRAFT status", testAssetId);
    }

    @Test
    @Order(2)
    void shouldTransitionToPendingReview() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/submit")
            .then()
            .statusCode(200)
            .body("id", equalTo(testAssetId))
            .body("status", equalTo("PENDING_REVIEW"));

        log.info("Asset {} transitioned to PENDING_REVIEW status", testAssetId);
    }

    @Test
    @Order(3)
    void shouldApproveAsset() {
        String requestBody = """
            {
                "comment": "审核通过",
                "approver": "admin"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/approve")
            .then()
            .statusCode(200)
            .body("id", equalTo(testAssetId))
            .body("status", equalTo("APPROVED"));

        log.info("Asset {} approved and transitioned to APPROVED status", testAssetId);
    }

    @Test
    @Order(4)
    void shouldPublishAsset() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/publish")
            .then()
            .statusCode(200)
            .body("id", equalTo(testAssetId))
            .body("status", equalTo("ACTIVE"));

        log.info("Asset {} published and transitioned to ACTIVE status", testAssetId);
    }

    @Test
    @Order(5)
    void shouldDeprecateAsset() {
        String requestBody = """
            {
                "reason": "资产已废弃",
                "effectiveTime": "2026-06-01T00:00:00Z"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/deprecate")
            .then()
            .statusCode(200)
            .body("id", equalTo(testAssetId))
            .body("status", equalTo("DEPRECATED"));

        log.info("Asset {} deprecated", testAssetId);
    }

    @Test
    @Order(6)
    void shouldRestoreDeprecatedAsset() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/restore")
            .then()
            .statusCode(200)
            .body("id", equalTo(testAssetId))
            .body("status", equalTo("ACTIVE"));

        log.info("Asset {} restored from deprecated status", testAssetId);
    }

    @Test
    @Order(7)
    void shouldArchiveAsset() {
        String requestBody = """
            {
                "archiveType": "COLD",
                "archiveTime": "2026-06-01T00:00:00Z"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/archive")
            .then()
            .statusCode(200)
            .body("id", equalTo(testAssetId))
            .body("status", equalTo("ARCHIVED"));

        log.info("Asset {} archived", testAssetId);
    }

    @Test
    @Order(8)
    void shouldGetLifecycleHistory() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/assets/" + testAssetId + "/lifecycle/history")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].assetId", equalTo(testAssetId))
            .body("[0].status", notNullValue())
            .body("[0].timestamp", notNullValue());
    }

    @Test
    @Order(9)
    void shouldPreventInvalidStatusTransition() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .post("/api/v1/assets/" + testAssetId + "/lifecycle/publish")
            .then()
            .statusCode(400)
            .body("code", equalTo(40002))
            .body("message", containsString("状态转换"));
    }

    @Test
    @Order(10)
    void shouldGetLifecycleTimeline() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/assets/" + testAssetId + "/lifecycle/timeline")
            .then()
            .statusCode(200)
            .body("assetId", equalTo(testAssetId))
            .body("events", hasSize(greaterThanOrEqualTo(1)))
            .body("events[0].phase", notNullValue())
            .body("events[0].status", notNullValue());
    }
}
