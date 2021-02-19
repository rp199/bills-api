package com.rp199.configuration

import com.rp199.configuration.properties.DynamoDBTableDefinitionsProperties
import com.rp199.configuration.properties.TableKeysProperties
import com.rp199.dsl.KeyType
import com.rp199.dsl.asyncTable
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.model.MonthlyBillDbBean
import com.rp199.repository.model.MonthlyBillGroupDbBean
import com.rp199.repository.model.UserDynamoDbBean
import com.rp199.repository.model.UserMonthlyBillsDbBean
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Dependent
import javax.ws.rs.Produces

@Dependent
class DynamoDBConfiguration(
        private val dynamoDBTableDefinitionsProperties: DynamoDBTableDefinitionsProperties,
        private val tableKeysProperties: TableKeysProperties
) {
    @Produces
    @ApplicationScoped
    fun dynamoDbAsyncEnhancedClient(dynamoDbAsyncClient: DynamoDbAsyncClient): DynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
            .dynamoDbClient(dynamoDbAsyncClient).build()

    @Produces
    @ApplicationScoped
    fun userDynamoDbBean(enhancedAsyncClient: DynamoDbEnhancedAsyncClient): DynamoDbAsyncTable<UserDynamoDbBean> {
        return asyncTable(UserDynamoDbBean::class.java, enhancedAsyncClient) {
            name = dynamoDBTableDefinitionsProperties.tableName
            schema {
                keys {
                    key(KeyType.PARTITION_KEY) {
                        name = dynamoDBTableDefinitionsProperties.hashKey
                        prefix = tableKeysProperties.userNamePrefix
                        prefixSeparator = dynamoDBTableDefinitionsProperties.keySeparator
                    }

                    key(KeyType.SORT_KEY) {
                        name = dynamoDBTableDefinitionsProperties.sortKey
                        getter { dynamoDBTableDefinitionsProperties.sortKeyPlaceHolder }
                    }
                }

                attributes {
                    attribute<String> {
                        name = "displayName"
                        getter { it.displayName }
                        setter { u, value -> u.displayName = value }
                    }
                }
            }
        }
    }

    @Produces
    @ApplicationScoped
    fun monthlyBillDynamoDbBean(enhancedAsyncClient: DynamoDbEnhancedAsyncClient): DynamoDbAsyncTable<MonthlyBillGroupDbBean> {
        return asyncTable(MonthlyBillGroupDbBean::class.java, enhancedAsyncClient) {
            name = dynamoDBTableDefinitionsProperties.tableName
            schema {
                keys {
                    key(KeyType.PARTITION_KEY) {
                        name = dynamoDBTableDefinitionsProperties.hashKey
                        prefix = tableKeysProperties.billsGroupPrefix
                        prefixSeparator = dynamoDBTableDefinitionsProperties.keySeparator
                    }

                    key(KeyType.SORT_KEY) {
                        name = dynamoDBTableDefinitionsProperties.sortKey
                        getter { dynamoDBTableDefinitionsProperties.sortKeyPlaceHolder }
                    }
                }

                attributes {
                    attribute<String> {
                        name = "description"
                        getter { it.description }
                        setter { u, value -> u.description = value!! }
                    }
                }
            }
        }
    }

    @Produces
    @ApplicationScoped
    fun userMonthlyBillsDbBean(enhancedAsyncClient: DynamoDbEnhancedAsyncClient): DynamoDbAsyncTable<UserMonthlyBillsDbBean> {
        return asyncTable(UserMonthlyBillsDbBean::class.java, enhancedAsyncClient) {
            name = dynamoDBTableDefinitionsProperties.tableName
            schema {
                keys {
                    key(KeyType.PARTITION_KEY) {
                        name = dynamoDBTableDefinitionsProperties.hashKey
                        prefix = tableKeysProperties.userNamePrefix
                        prefixSeparator = dynamoDBTableDefinitionsProperties.keySeparator
                    }

                    key(KeyType.SORT_KEY) {
                        name = dynamoDBTableDefinitionsProperties.sortKey
                        prefix = tableKeysProperties.billsGroupPrefix
                        prefixSeparator = dynamoDBTableDefinitionsProperties.keySeparator
                    }
                }
            }
        }
    }

    @Produces
    @ApplicationScoped
    fun monthlyBill(enhancedAsyncClient: DynamoDbEnhancedAsyncClient): DynamoDbAsyncTable<MonthlyBillDbBean> {
        return asyncTable(MonthlyBillDbBean::class.java, enhancedAsyncClient) {
            name = dynamoDBTableDefinitionsProperties.tableName
            schema {
                keys {
                    key(KeyType.PARTITION_KEY) {
                        name = dynamoDBTableDefinitionsProperties.hashKey
                        prefix = tableKeysProperties.billsGroupPrefix
                        prefixSeparator = dynamoDBTableDefinitionsProperties.keySeparator
                    }

                    key(KeyType.SORT_KEY) {
                        name = dynamoDBTableDefinitionsProperties.sortKey
                        prefix = tableKeysProperties.yearMonthPrefix
                        prefixSeparator = dynamoDBTableDefinitionsProperties.keySeparator
                    }
                }

                attributes {
                    attribute<String> {
                        name = "bills"
                        getter { it.bills }
                        setter { u, value -> u.bills = value!! }
                    }
                }
            }
        }
    }

    @Produces
    @ApplicationScoped
    fun userRepository(userDynamoDbBean: DynamoDbAsyncTable<UserDynamoDbBean>): DynamoDBRepository<String, String, UserDynamoDbBean> {
        return DynamoDBRepository(userDynamoDbBean, dynamoDBTableDefinitionsProperties)
    }

    @Produces
    @ApplicationScoped
    fun monthlyBillGroupRepository(monthlyBillDynamoDbBean: DynamoDbAsyncTable<MonthlyBillGroupDbBean>): DynamoDBRepository<String, String, MonthlyBillGroupDbBean> {
        return DynamoDBRepository(monthlyBillDynamoDbBean, dynamoDBTableDefinitionsProperties)
    }

    @Produces
    @ApplicationScoped
    fun monthlyBillRepository(monthlyBill: DynamoDbAsyncTable<MonthlyBillDbBean>): DynamoDBRepository<String, String, MonthlyBillDbBean> {
        return DynamoDBRepository(monthlyBill, dynamoDBTableDefinitionsProperties)
    }

    @Produces
    @ApplicationScoped
    fun userMonthlyBillsRepository(userMonthlyBillsDbBean: DynamoDbAsyncTable<UserMonthlyBillsDbBean>): DynamoDBRepository<String, String, UserMonthlyBillsDbBean> {
        return DynamoDBRepository(userMonthlyBillsDbBean, dynamoDBTableDefinitionsProperties)
    }
}