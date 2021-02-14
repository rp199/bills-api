package com.rp199.resource

import com.rp199.model.CreateUserRequest
import com.rp199.model.PutUserRequest
import com.rp199.model.UpdateUserRequest
import com.rp199.model.UserAlreadyExists
import com.rp199.model.UserCreated
import com.rp199.repository.model.UserDynamoDbBean
import com.rp199.service.UserService
import kotlinx.coroutines.runBlocking
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import java.net.URI
import javax.validation.Valid
import javax.validation.constraints.Pattern
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.PATCH
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response


@Path("/users")
class UserResource(val userService: UserService, val table: DynamoDbAsyncTable<UserDynamoDbBean>) {

    @GET
    @Path("/{userName}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUser(@PathParam("userName") userName: String): Response = runBlocking {
        userService.getUser(userName)?.let {
            Response.ok(it).build()
        } ?: Response.status(Response.Status.NOT_FOUND).build()
    }

    @PUT
    @Path("/{userName}")
    @Consumes(MediaType.APPLICATION_JSON)
    fun putUser(@PathParam("userName") @Pattern(regexp = "^[a-zA-Z0-9]*\$") userName: String, @Valid putUserRequest: PutUserRequest): Response = runBlocking {
        when (val result = userService.createOrUpdateUser(userName, putUserRequest)) {
            is UserCreated -> Response.created(URI.create("/user/${result.userName}")).build()
            UserAlreadyExists -> Response.status(Response.Status.NO_CONTENT).build()
        }
    }

    @DELETE
    @Path("/{userName}")
    fun deleteUser(@PathParam("userName") userName: String): Response = runBlocking {
        userService.deleteUser(userName)
        Response.noContent().build()
    }

    @PATCH
    @Path("/{userName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun patchUser(@PathParam("userName") userName: String, @Valid updateUserRequest: UpdateUserRequest): Response = runBlocking {
        userService.updateUser(userName, updateUserRequest)?.let {
            Response.ok(it).build()
        } ?: Response.status(Response.Status.NOT_FOUND).build()

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun createUser(@Valid createUserRequest: CreateUserRequest): Response = runBlocking {
        when (val result = userService.createUser(createUserRequest)) {
            is UserCreated -> Response.created(URI.create("/user/${result.userName}")).build()
            UserAlreadyExists -> Response.status(Response.Status.CONFLICT).build()
        }
    }
}