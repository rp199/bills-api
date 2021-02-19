package com.rp199.resource

import java.net.URI
import javax.ws.rs.core.Response


fun <T> T?.okOrNotFound(): Response = this?.let { Response.ok(this).build() }
        ?: Response.status(Response.Status.NOT_FOUND).build()

fun String.created(baseLocation: String): Response = Response.created(URI.create("$baseLocation/$this")).build()

fun noContent(): Response = Response.noContent().build()

fun conflict(): Response = Response.status(Response.Status.CONFLICT).build()