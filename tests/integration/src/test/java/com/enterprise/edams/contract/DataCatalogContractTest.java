package com.enterprise.edams.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.AutoConfigureMessageVerifier;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.springframework.cloud.contract.verifier.messaging.util.ContractVerifierMessagingUtil.headers;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMessageVerifier
@ActiveProfiles("test")
public class DataCatalogContractTest {

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @Test
    void shouldReturnAssetDetails() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/v1/assets/" + assetId)
            .then()
            .statusCode(200)
            .body("id", equalTo(assetId))
            .body("name", notNullValue())
            .body("assetType", notNullValue())
            .body("status", notNullValue());
    }

    @Test
    void shouldReturnAssetList() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("page", 0)
            .param("size", 10)
            .when()
            .get("/api/v1/assets")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThanOrEqualTo(0)))
            .body("totalElements", greaterThanOrEqualTo(0))
            .body("totalPages", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldCreateNewAsset() {
        String requestBody = """
            {
                "name": "Test Asset",
                "assetType": "TABLE",
                "description": "Contract test asset",
                "owner": "admin",
                "sensitivityLevel": "INTERNAL"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .post("/api/v1/assets")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Asset"))
            .body("assetType", equalTo("TABLE"))
            .body("status", equalTo("DRAFT"));
    }

    @Test
    void shouldUpdateAsset() {
        String assetId = "test-asset-001";
        String requestBody = """
            {
                "name": "Updated Asset Name",
                "description": "Updated description"
            }
            """;

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody)
            .when()
            .put("/api/v1/assets/" + assetId)
            .then()
            .statusCode(200)
            .body("id", equalTo(assetId))
            .body("name", equalTo("Updated Asset Name"));
    }

    @Test
    void shouldDeleteAsset() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/api/v1/assets/" + assetId)
            .then()
            .statusCode(204);
    }

    @Test
    void shouldReturnAssetNotFound() {
        String nonExistentAssetId = "non-existent-id";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/v1/assets/" + nonExistentAssetId)
            .then()
            .statusCode(404)
            .body("code", equalTo(20001))
            .body("message", containsString("不存在"));
    }

    @Test
    void shouldSearchAssets() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("keyword", "test")
            .when()
            .get("/api/v1/assets/search")
            .then()
            .statusCode(200)
            .body("content", notNullValue());
    }

    @Test
    void shouldGetAssetFields() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/v1/assets/" + assetId + "/fields")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void shouldFavoriteAsset() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api/v1/assets/" + assetId + "/favorite")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Test
    void shouldGetAssetQuality() {
        String assetId = "test-asset-001";

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/v1/assets/" + assetId + "/quality")
            .then()
            .statusCode(200)
            .body("assetId", equalTo(assetId))
            .body("qualityScore", notNullValue());
    }

    @Test
    void shouldGetAssetStatistics() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/v1/assets/statistics/overview")
            .then()
            .statusCode(200)
            .body("totalAssets", greaterThanOrEqualTo(0))
            .body("activeAssets", greaterThanOrEqualTo(0));
    }

    @Test
    void shouldValidateAssetType() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .param("assetType", "INVALID_TYPE")
            .when()
            .get("/api/v1/assets")
            .then()
            .statusCode(400);
    }

    @Test
    void shouldRequireAuthentication() {
        given()
            .when()
            .get("/api/v1/assets")
            .then()
            .statusCode(401);
    }
}
