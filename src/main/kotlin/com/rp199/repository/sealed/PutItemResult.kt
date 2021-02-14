package com.rp199.repository.sealed

import com.rp199.repository.model.DynamoDbBean

sealed class PutItemResult<S, T>(val dynamoDbBean: DynamoDbBean<S, T>)

class KeysAlreadyOnTheDatabase<S, T>(dynamoDbBean: DynamoDbBean<S, T>) : PutItemResult<S, T>(dynamoDbBean)

class ItemCreated<S, T>(dynamoDbBean: DynamoDbBean<S, T>) : PutItemResult<S, T>(dynamoDbBean)