package com.rp199.resource

import com.rp199.model.CreateMonthlyBillsGroupRequest
import com.rp199.service.MonthlyBillsService
import kotlinx.coroutines.runBlocking
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/monthly-bills")
class MonthlyBillsResource(private val monthlyBillsService: MonthlyBillsService) {

    @GET
    @Path("/groups/{id}")
    fun getMonthlyBillGroup(@PathParam("id") id: String): Response = runBlocking {
        monthlyBillsService.getMonthlyBillsGroup(id)
                .okOrNotFound()
    }

    @POST
    @Path("/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    fun createMonthlyBillGroup(request: CreateMonthlyBillsGroupRequest): Response = runBlocking {
        monthlyBillsService.createMonthlyBillsGroup(request.description, request.users)
                .created("/monthly-bills/groups")
    }
}