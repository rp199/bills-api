package com.rp199

import com.rp199.model.MonthlyBillsOverview
import com.rp199.model.UserResponse
import com.rp199.service.UserService
import io.mockk.coEvery
import io.mockk.mockk
import io.quarkus.test.junit.QuarkusMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

val userServiceMock: UserService = mockk()

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserResourceTest {

    @BeforeAll
    fun setup() {
        QuarkusMock.installMockForType(userServiceMock, UserService::class.java)
    }

    @Test
    fun `GET users by userName should return 404 when not found`() {
        val userName = "someUserName"
        coEvery { userServiceMock.getUser(userName) } returns null

        given()
                .`when`().get("users/{userName}", userName)
                .then()
                .statusCode(404)
    }

    @Test
    fun `GET users by userName should return 200 and user when found`() {
        val userName = "someUserName"
        val expectedResponse = UserResponse(userName, "Mr User",
                listOf(MonthlyBillsOverview("123", "Electrical Bill")))

        coEvery { userServiceMock.getUser(any()) } returns expectedResponse

        given()
                .`when`().get("users/{userName}", userName)
                .then()
                .statusCode(200)
                
    }

}