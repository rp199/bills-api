package com.rp199.configuration

import com.rp199.configuration.properties.DynamoDBTableDefinitionsProperties
import com.rp199.configuration.properties.TableKeysProperties
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.TablePrefixedKey
import com.rp199.repository.model.UserViewModel
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import javax.enterprise.context.Dependent
import javax.inject.Named
import javax.inject.Singleton
import javax.ws.rs.Produces

@Dependent
class DynamoDBConfiguration(
        private val dynamoDBTableDefinitionsProperties: DynamoDBTableDefinitionsProperties,
        private val tableKeysProperties: TableKeysProperties
) {

    @Produces
    @Named
    @Singleton
    fun userNamePrefixedKey(): TablePrefixedKey {
        return TablePrefixedKey(tableKeysProperties.userNamePrefix,
                tableKeysProperties.billsGroupPrefix, tableKeysProperties.keySeparator)
    }

    @Produces
    @Named
    @Singleton
    fun billsGroupPrefixedKey(): TablePrefixedKey {
        return TablePrefixedKey(tableKeysProperties.billsGroupPrefix,
                tableKeysProperties.userNamePrefix, tableKeysProperties.keySeparator)
    }

    @Produces
    @Named
    @Singleton
    fun userRepository(dynamoDbAsyncClient: DynamoDbAsyncClient,
                       @Named("userNamePrefixedKey") userNamePrefixedKey: TablePrefixedKey): DynamoDBRepository<UserViewModel> {
        return DynamoDBRepository(dynamoDbAsyncClient, dynamoDBTableDefinitionsProperties, userNamePrefixedKey) {
            UserViewModel.fromDynamoDDRecord(it)
        }
    }

}