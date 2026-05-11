package com.enterprise.edams.integration;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LineageIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(LineageIntegrationTest.class);

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @Test
    @Order(1)
    void shouldGetTableLineage() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/lineage/table/" + assetId)
            .then()
            .statusCode(200)
            .body("assetId", equalTo(assetId))
            .body("nodes", notNullValue())
            .body("edges", notNullValue());
    }

    @Test
    @Order(2)
    void shouldGetFieldLineage() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/lineage/field/" + assetId)
            .then()
            .statusCode(200)
            .body("assetId", equalTo(assetId))
            .body("fields", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(3)
    void shouldGetLineagePath() {
        String requestBody = """
            {
                "sourceAssetId": "source-001",
                "targetAssetId": "target-001"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/lineage/path")
            .then()
            .statusCode(200)
            .body("path", notNullValue())
            .body("distance", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(4)
    void shouldGetLineageGraph() {
        String requestBody = """
            {
                "centerAssetId": "test-asset-001",
                "depth": 2
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/lineage/graph")
            .then()
            .statusCode(200)
            .body("nodes", notNullValue())
            .body("edges", notNullValue());
    }

    @Test
    @Order(5)
    void shouldGetImpactAnalysis() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/lineage/impact/" + assetId)
            .then()
            .statusCode(200)
            .body("assetId", equalTo(assetId))
            .body("affectedAssets", notNullValue())
            .body("downstreamAssets", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(6)
    void shouldGetDependencyAnalysis() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/lineage/dependency/" + assetId)
            .then()
            .statusCode(200)
            .body("assetId", equalTo(assetId))
            .body("dependencies", notNullValue())
            .body("upstreamAssets", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(7)
    void shouldCreateLineageRelation() {
        String requestBody = """
            {
                "sourceAssetId": "source-001",
                "sourceFieldId": "field-001",
                "targetAssetId": "target-001",
                "targetFieldId": "field-002",
                "transformType": "DIRECT_MAPPING",
                "description": "字段映射关系"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/lineage")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("sourceAssetId", equalTo("source-001"));
    }

    @Test
    @Order(8)
    void shouldDeleteLineageRelation() {
        String lineageId = "lineage-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .delete("/api/v1/lineage/" + lineageId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(9)
    void shouldVerifyLineage() {
        String requestBody = """
            {
                "assetId": "test-asset-001"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/lineage/verify")
            .then()
            .statusCode(200)
            .body("valid", notNullValue())
            .body("issues", notNullValue());
    }

    @Test
    @Order(10)
    void shouldGetLineageStatistics() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/lineage/statistics")
            .then()
            .statusCode(200)
            .body("totalRelations", greaterThanOrEqualTo(0))
            .body("totalAssets", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(11)
    void shouldGetLineageHistory() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/v1/lineage/" + assetId + "/history")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(12)
    void shouldCompareLineage() {
        String requestBody = """
            {
                "assetIds": ["asset-001", "asset-002"]
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/lineage/compare")
            .then()
            .statusCode(200)
            .body("commonRelations", notNullValue())
            .body("uniqueRelations", notNullValue());
    }

    @Test
    @Order(13)
    void shouldRequireAuthForLineage() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/v1/lineage/table/test-asset-001")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(14)
    void shouldHandleCircularLineage() {
        String requestBody = """
            {
                "sourceAssetId": "asset-A",
                "targetAssetId": "asset-B"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer test-token")
            .body(requestBody)
            .when()
            .post("/api/v1/lineage/path")
            .then()
            .statusCode(200)
            .body("hasCircularDependency", notNullValue());
    }
}
