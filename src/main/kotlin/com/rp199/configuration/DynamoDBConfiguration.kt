package com.rp199.configuration

import com.rp199.configuration.properties.DynamoDBTableDefinitionsProperties
import com.rp199.configuration.properties.TableKeysProperties
import com.rp199.dsl.KeyType
import com.rp199.dsl.asyncTable
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.model.UserDynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import javax.enterprise.context.Dependent
import javax.inject.Singleton
import javax.ws.rs.Produces

@Dependent
class DynamoDBConfiguration(
        private val dynamoDBTableDefinitionsProperties: DynamoDBTableDefinitionsProperties,
        private val tableKeysProperties: TableKeysProperties
) {
    @Produces
    @Singleton
    fun dynamoDbAsyncEnhancedClient(dynamoDbAsyncClient: DynamoDbAsyncClient): DynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
            .dynamoDbClient(dynamoDbAsyncClient).build()

    @Produces
    @Singleton
    fun userDynamoDbBean(enhancedAsyncClient: DynamoDbEnhancedAsyncClient): DynamoDbAsyncTable<UserDynamoDbBean> {
        return asyncTable(UserDynamoDbBean::class.java, enhancedAsyncClient) {
            name = dynamoDBTableDefinitionsProperties.tableName
            schema {
                keys {
                    key(KeyType.PARTITION_KEY) {
                        name = dynamoDBTableDefinitionsProperties.hashKey
                        prefix = tableKeysProperties.userNamePrefix
                        prefixSeparator = tableKeysProperties.keySeparator
                    }

                    key(KeyType.SORT_KEY) {
                        name = dynamoDBTableDefinitionsProperties.sortKey
                        getter {
                            dynamoDBTableDefinitionsProperties.sortKeyPlaceHolder
                        }
                    }
                }

                attributes {
                    attribute<String> {
                        name = "displayName"
                        getter {
                            it.displayName
                        }
                        setter { u, value ->
                            u.displayName = value
                        }
                    }
                }
            }
        }
    }


    @Produces
    @Singleton
    fun userRepository(table: DynamoDbAsyncTable<UserDynamoDbBean>): DynamoDBRepository<String, String, UserDynamoDbBean> {
        return DynamoDBRepository(table, dynamoDBTableDefinitionsProperties)
    }

}