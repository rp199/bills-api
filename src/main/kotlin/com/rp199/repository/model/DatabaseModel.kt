package com.rp199.repository.model

import com.rp199.repository.DynamoDBRecord
import software.amazon.awssdk.services.dynamodb.model.AttributeValue as AWSAttributeValue


interface DynamoDBViewModel{
    fun toDynamoDBRecord(): DynamoDBRecord
}

//TODO naming pending
data class UserViewModel(val userName: String, val displayName: String) : DynamoDBViewModel {
    companion object {
        const val DISPLAY_NAME_ATTRIBUTE = "displayName"
        fun fromDynamoDDRecord(dynamoDBRecord: DynamoDBRecord): UserViewModel {
            return UserViewModel(dynamoDBRecord.pk, dynamoDBRecord.attributes[DISPLAY_NAME_ATTRIBUTE] ?.s()?: dynamoDBRecord.pk)
        }
    }

    override fun toDynamoDBRecord(): DynamoDBRecord {
        return DynamoDBRecord(userName, attributes = mapOf(
                DISPLAY_NAME_ATTRIBUTE to AttributeValue.string(displayName)
        ))
    }


}

class AttributeValue {
    companion object{
        fun string(stringValue: String): AWSAttributeValue {
            return AWSAttributeValue.builder().s(stringValue).build()
        }
    }
}