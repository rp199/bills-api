package com.rp199.repository

import com.rp199.configuration.properties.DynamoDBTableDefinitionsProperties
import com.rp199.repository.model.DynamoDBViewModel
import com.rp199.repository.sealed.ItemCreated
import com.rp199.repository.sealed.KeysAlreadyOnTheDatabase
import com.rp199.repository.sealed.PutItemResult
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException
import software.amazon.awssdk.services.dynamodb.model.ReturnValue
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType

class DynamoDBRepository<T : DynamoDBViewModel>(
        private val dynamoDbAsyncClient: DynamoDbAsyncClient,
        private val definitions: DynamoDBTableDefinitionsProperties,
        private val tablePrefixedKey: TablePrefixedKey,
        private val mappingFunction: (DynamoDBRecord) -> T
) {

    suspend fun getItemView(pk: String, sk: String? = null): T? {
        return getItem(pk, sk)?.let { mappingFunction(it) }
    }


    suspend fun putItemViewIfNotExists(dynamoDBViewModel: T): PutItemResult {
        val dynamoDBRecord = dynamoDBViewModel.toDynamoDBRecord()
        return putItemIfNotExists(dynamoDBRecord.pk, dynamoDBRecord.sk, dynamoDBRecord.attributes)
    }

    suspend fun putItemView(dynamoDBViewModel: T): PutItemResult {
        val dynamoDBRecord = dynamoDBViewModel.toDynamoDBRecord()
        val putItemResult: PutItemResult = getItem(dynamoDBRecord.pk, dynamoDBRecord.sk)?.let {
            ItemCreated(dynamoDBRecord.pk, dynamoDBRecord.sk)
        } ?: KeysAlreadyOnTheDatabase(dynamoDBRecord.pk, dynamoDBRecord.sk)
        this.putItem(dynamoDBRecord.pk, dynamoDBRecord.sk)

        return putItemResult
    }

    suspend fun deleteItemView(pk: String, sk: String? = null) {
        dynamoDbAsyncClient.deleteItem {
            it.tableName(definitions.tableName)
                    .key(buildKeysAttributes(pk, sk))
        }.await()
    }

    suspend fun updateItem(dynamoDBRecord: DynamoDBRecord): T? {
        return try {
            return dynamoDbAsyncClient.updateItem {
                it.tableName(definitions.tableName)
                        .key(buildKeysAttributes(dynamoDBRecord.pk, dynamoDBRecord.sk))
                        .attributeUpdates(dynamoDBRecord.attributes.mapValues { (_, value) ->
                            AttributeValueUpdate.builder().value(value).build()
                        }).returnValues(ReturnValue.ALL_NEW)
            }.await().attributes().toDynamoDbRecord().let { mappingFunction(it) }
        } catch (e: ResourceNotFoundException) {
            null
        }
    }

    private suspend fun getItem(pk: String, sk: String? = null): DynamoDBRecord? {
        return dynamoDbAsyncClient.getItem {
            it.tableName(definitions.tableName)
                    .keysOnly(pk, sk)
        }.await().takeIf { it.hasItem() }?.item()?.toDynamoDbRecord()
    }

    private suspend fun putItemIfNotExists(pk: String, sk: String? = null, attributeValues: Map<String, AttributeValue> = mapOf()): PutItemResult {
        return try {
            this.putItem(pk, sk, attributeValues) {
                it.conditionExpression("attribute_not_exists(${definitions.hashKey})")
            }
            ItemCreated(pk, sk)
        } catch (e: ConditionalCheckFailedException) {
            KeysAlreadyOnTheDatabase(pk, sk)
        }
    }

    private suspend fun putItem(pk: String, sk: String? = null, attributeValues: Map<String, AttributeValue> = mapOf(),
                                additionalOpts: (PutItemRequest.Builder) -> Unit = {}) {
        dynamoDbAsyncClient.putItem {
            additionalOpts(
                    it.tableName(definitions.tableName)
                            .item(buildKeysAttributes(pk, sk) + attributeValues)
            )
        }.await()
    }

    private fun GetItemRequest.Builder.keysOnly(pk: String, sk: String? = null): GetItemRequest.Builder {
        return this.key(buildKeysAttributes(pk, sk))
    }

    private fun buildKeysAttributes(pk: String, sk: String? = null): Map<String, AttributeValue> {
        return mapOf(
                definitions.hashKey to AttributeValue.builder().s(tablePrefixedKey.getPrefixedPk(pk)).build(),
                definitions.sortKey to AttributeValue.builder().s(sk?.let { tablePrefixedKey.getPrefixedSk(it) }
                        ?: definitions.sortKeyPlaceHolder
                        ?: throw IllegalArgumentException("this view don't support no value sk")).build()
        )
    }

    private fun Map<String, AttributeValue>.toDynamoDbRecord(): DynamoDBRecord {
        return DynamoDBRecord(checkNotNull(this[definitions.hashKey]).s(), checkNotNull(this[definitions.sortKey]).s(), this.otherAttributes())
    }

    private fun Map<String, AttributeValue>.otherAttributes(): Map<String, AttributeValue> {
        return this.filterKeys { it !in listOf(definitions.hashKey, definitions.sortKey) }
    }

    //TODO to remove
    suspend fun createTable(tableName: String) {
        val keys = listOf(
                KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("pk").build(),
                KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName("sk").build()
        )

        val localSecondaryIndexSchema = listOf(
                KeySchemaElement.builder().keyType(KeyType.HASH).attributeName("sk").build(),
                KeySchemaElement.builder().keyType(KeyType.RANGE).attributeName("pk").build()
        )

        val attributes = listOf(
                AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build(),
        )


        dynamoDbAsyncClient.createTable { t ->
            t.tableName("bills")
                    .keySchema(keys)
                    .attributeDefinitions(attributes)
                    .provisionedThroughput {
                        it.readCapacityUnits(5).writeCapacityUnits(5)
                    }
            //.localSecondaryIndexes(LocalSecondaryIndex.builder()
            //        .indexName("b-group-user-index")
            //        .keySchema(localSecondaryIndexSchema)
            //        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
            //        .build()
            //)

        }.await()
    }
}