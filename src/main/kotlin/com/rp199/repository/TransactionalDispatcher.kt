package com.rp199.repository

import kotlinx.coroutines.future.await
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class TransactionalDispatcher(private val asyncClient: DynamoDbEnhancedAsyncClient) {

    suspend fun doWriteWithTransaction(builder: List<(TransactWriteItemsEnhancedRequest.Builder) -> Unit>) {
        asyncClient.transactWriteItems { b ->
            builder.forEach { b.apply(it) }
        }.await()
    }
}