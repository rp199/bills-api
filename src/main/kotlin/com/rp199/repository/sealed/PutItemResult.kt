package com.rp199.repository.sealed

sealed class PutItemResult

data class KeysAlreadyOnTheDatabase(val pk: String, val sk: String?) : PutItemResult()

data class ItemCreated(val pk: String, val sk: String?) : PutItemResult()