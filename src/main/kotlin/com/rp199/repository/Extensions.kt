package com.rp199.repository

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

//Workaround to get the prefixes at runtime
fun <T> DynamoDbAsyncTable<T>.keyPrefixTuple(hashKey: String, sortKey: String, keySeparator: String): Pair<String, String> {
    val keyNames = listOf(hashKey, sortKey)
    val dummy = "dummy"
    val item = tableSchema().mapToItem(
            mapOf(
                    keyNames.first() to AttributeValue.builder().s(dummy).build(),
                    keyNames[1] to AttributeValue.builder().s(dummy).build(),
            )
    )

    return tableSchema().itemToMap(item, keyNames)
            .let {
                val pk = it[keyNames.first()]?.s() ?: ""
                val sk = it[keyNames[1]]?.s() ?: ""
                pk to sk
            }.removeSuffix(dummy, keySeparator)
}

private fun Pair<String, String>.removeSuffix(keyValue: String, keySeparator: String): Pair<String, String> {
    return first.removeSuffix("$keySeparator$keyValue") to second.removeSuffix("$keySeparator$keyValue")
}