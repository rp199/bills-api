package com.rp199.model

import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

annotation class JavaEmptyConstructor

@JavaEmptyConstructor
data class CreateUserRequest(@get:Size(min = 3, max = 10) @get:Pattern(regexp = "^[a-zA-Z0-9]*$") val userName: String, @get:Size(min = 3, max = 10) val displayName: String?)

@JavaEmptyConstructor
data class PutUserRequest(@get:Size(min = 3, max = 10) val displayName: String)

@JavaEmptyConstructor
data class UpdateUserRequest(@get:Size(min = 3, max = 10) val displayName: String?)

@JavaEmptyConstructor
data class CreateMonthlyBillsGroupRequest(val description: String, val users: List<String>)