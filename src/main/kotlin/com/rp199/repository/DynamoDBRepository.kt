package com.rp199.repository

import com.rp199.configuration.properties.DynamoDBTableDefinitionsProperties
import com.rp199.repository.model.DynamoDbBean
import com.rp199.repository.sealed.ItemCreated
import com.rp199.repository.sealed.KeysAlreadyOnTheDatabase
import com.rp199.repository.sealed.PutItemResult
import kotlinx.coroutines.future.await
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException

class DynamoDBRepository<K, S, T : DynamoDbBean<K, S>>(
        private val table: DynamoDbAsyncTable<T>,
        private val definitionsProperties: DynamoDBTableDefinitionsProperties
) {

    suspend fun getItem(keyItem: T): T? {
        return table.getItem(keyItem).await()
    }

    suspend fun putItem(keyItem: T) {
        table.putItem(keyItem)
                .await()
    }

    suspend fun putItemIfNotExists(item: T): PutItemResult<K, S> {
        return try {
            table.putItem {
                it.item(item).conditionExpression(
                        Expression.builder()
                                .expression("attribute_not_exists(#${definitionsProperties.hashKey})")
                                .expressionNames(mapOf("#${definitionsProperties.hashKey}" to definitionsProperties.hashKey))
                                .build()
                )
            }.await()
            ItemCreated(item)
        } catch (e: ConditionalCheckFailedException) {
            KeysAlreadyOnTheDatabase(item)
        }

    }

    suspend fun updateItem(item: T): T? {
        return try {
            table.updateItem {
                it.item(item).ignoreNulls(true)
                        .conditionExpression(
                                Expression.builder()
                                        .expression("attribute_exists(#${definitionsProperties.hashKey})")
                                        .expressionNames(mapOf("#${definitionsProperties.hashKey}" to definitionsProperties.hashKey))
                                        .build())
            }.await()
        } catch (e: ConditionalCheckFailedException) {
            null
        }
    }

    suspend fun deleteItem(item: T) {
        table.deleteItem(item).await()
    }
}