package com.rp199.model

sealed class PutUserResult

data class UserCreated(val userName: String) : PutUserResult()

object UserAlreadyExists : PutUserResult()