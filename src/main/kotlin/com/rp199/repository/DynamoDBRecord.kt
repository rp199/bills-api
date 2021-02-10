package com.rp199.repository

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class DynamoDBRecord(val pk: String, val sk: String? = null, val attributes: Map<String, AttributeValue>)
