package com.rp199

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

@QuarkusTest
class BillsResourceTest {

    @Test
    fun testHelloEndpoint() {
        given()
                .`when`().get("users/{userName}", "teste")
                .then()
        //.statusCode(200)
        //.body(`is`("teste"))
    }

}