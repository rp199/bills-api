package com.rp199.model

import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


data class CreateUserRequest(@get:Size(min = 3, max = 10) @get:Pattern(regexp = "^[a-zA-Z0-9]*$") val userName: String, @get:Size(min = 3, max = 10) val displayName: String?)
data class PutUserRequest(@get:Size(min = 3, max = 10) val displayName: String)
data class UpdateUserRequest(@get:Size(min = 3, max = 10) val displayName: String?)