package org.acme.opentelemetry;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TracedResourceTest {

    @Test
    public void testSayHelloEndpoint() {
        given()
                .when().get("/sayHello/test")
                .then()
                .statusCode(200)
                .body(is("hello: test"));
    }

    @Test
    public void testSayRemoteEndpoint() {
        given()
                .when().get("/sayRemote/test")
                .then()
                .statusCode(200)
                .body(is("hello: test from http://localhost:8081/"));
    }
    
    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("hello"));
    }

    @Test
    public void testChainEndpoint() {
        given()
                .when().get("/chain")
                .then()
                .statusCode(200)
                .body(is("chain -> hello: test"));
    }

    @Test
    public void primeEndpoint() {
        given()
                .when().get("/prime/2791")
                .then()
                .statusCode(200)
                .body(is("2791 is a prime."));
    }

    @Test
    public void noprimeEndpoint() {
        given()
                .when().get("/prime/121")
                .then()
                .statusCode(200)
                .body(is("121 is not a prime, is divisible by 11."));
    }
}
