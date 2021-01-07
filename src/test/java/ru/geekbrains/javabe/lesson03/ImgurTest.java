package ru.geekbrains.javabe.lesson03;

import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ImgurTest {
    private static String IMAGE_API_URL = "https://api.imgur.com/3/image";
    private static Map<String, String> headers = new HashMap<>();

    @BeforeAll
    static void setUp() {
        headers.put("Authorization", "Bearer fcac379200db5faa83dcc6007644d0f0eebdff2f");
    }

    @Test
    void uploadAndDelete() {
        String imageDeleteHash = given()
                .headers(headers)
                .multiPart("image", "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7")
                .expect()
                .body("success", is(true))
                .body("data.id", is(notNullValue()))
                .when()
                .post(IMAGE_API_URL)
                .prettyPeek()
                .then()
                .statusCode(200)
                .extract()
                .response()
                .jsonPath()
                .getString("data.deletehash");

        given()
                .headers(headers)
                .expect()
                .body("success", is(true))
                .when()
                .delete(IMAGE_API_URL + "/{deleteHash}", imageDeleteHash)
                .prettyPeek()
                .then()
                .statusCode(200);
    }

    @Test
    void uploadAndUpdateAndGet() {
        Response resp = given()
                .headers(headers)
                .multiPart("image", "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7")
                .multiPart("title", "Old title")
                .multiPart("description", "Old description")
                .expect()
                .body("success", is(true))
                .body("data.id", is(notNullValue()))
                .when()
                .post(IMAGE_API_URL)
                .prettyPeek()
                .then()
                .statusCode(200)
                .extract()
                .response();
        String imageHash = resp.jsonPath().getString("data.id");
        String imageDeleteHash = resp.jsonPath().getString("data.deletehash");

        given()
                .headers(headers)
                .multiPart("title", "New title")
                .multiPart("description", "New description")
                .expect()
                .body("success", is(true))
                .when()
                .post(IMAGE_API_URL + "/{deleteHash}", imageDeleteHash)
                .prettyPeek()
                .then()
                .statusCode(200);

        given()
                .headers(headers)
                .expect()
                .body("success", is(true))
                .body("data.id", is(imageHash))
                .body("data.title", is("New title"))
                .body("data.description", is("New description"))
                .when()
                .get(IMAGE_API_URL + "/{idHash}", imageHash)
                .prettyPeek()
                .then()
                .statusCode(200);

        given()
                .headers(headers)
                .expect()
                .body("success", is(true))
                .when()
                .delete(IMAGE_API_URL + "/{deleteHash}", imageDeleteHash)
                .prettyPeek()
                .then()
                .statusCode(200);
    }

    @Test
    void uploadBadContent() {
        given()
                .headers(headers)
                .multiPart("image", "invalid image")
                .expect()
                .body("success", is(false))
                .body("data.id", is(nullValue()))
                .when()
                .post(IMAGE_API_URL)
                .prettyPeek()
                .then()
                .statusCode(400);
    }

    @Test
    void uploadBase64NotImage() {
        given()
                .headers(headers)
                .multiPart("image", "dGhpcyBjb250ZW50IGlzIG5vdCB2YWxpZCBpbWFnZSBqdXN0IGZvciB0ZXN0cw==")
                .expect()
                .body("success", is(false))
                .body("data.id", is(nullValue()))
                .when()
                .post(IMAGE_API_URL)
                .prettyPeek()
                .then()
                .statusCode(400);
    }

    @Test
    void uploadUrlToHtml() {
        given()
                .headers(headers)
                .multiPart("image", "https://ya.ru/")
                .expect()
                .body("success", is(false))
                .body("data.id", is(nullValue()))
                .body("data.error.type", is("ImgurException"))
                .when()
                .post(IMAGE_API_URL)
                .prettyPeek()
                .then()
                .statusCode(400);
    }

    @Test
    void uploadTooLarge() {
        given()
                .headers(headers)
                .multiPart("image", "https://sample-videos.com/img/Sample-jpg-image-10mb.jpg")
                .expect()
                .body("success", is(false))
                .body("data.id", is(nullValue()))
                .body("data.error", is("File is over the size limit"))
                .when()
                .post(IMAGE_API_URL)
                .prettyPeek()
                .then()
                .statusCode(400);
    }

    @Test
    void deleteUnknownHash() {
        given()
                .headers(headers)
                .expect()
                .body("success", is(false))
                .when()
                .delete(IMAGE_API_URL + "/invalidhash")
                .prettyPeek()
                .then()
                .statusCode(403);
    }

    @Test
    void getUnknownHash() {
        given()
                .headers(headers)
                .when()
                .get(IMAGE_API_URL + "/invalidhash")
                .prettyPeek()
                .then()
                .statusCode(404);
    }

    @Test
    void addToFavoriteUnknownHash() {
        given()
                .headers(headers)
                .when()
                .post(IMAGE_API_URL + "/invalidhash/favorite")
                .prettyPeek()
                .then()
                .statusCode(404);
    }

    @Test
    void updateUnknownHash() {
        given()
                .headers(headers)
                .multiPart("title", "Heart")
                .multiPart("description", "This is an image of a heart outline.")
                .when()
                .post(IMAGE_API_URL + "/invalidhash")
                .prettyPeek()
                .then()
                .statusCode(404);
    }
}
