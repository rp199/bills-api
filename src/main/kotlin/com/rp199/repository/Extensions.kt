package com.rp199.repository

import software.amazon.awssdk.services.dynamodb.model.AttributeValue


fun AttributeValue.string(stringValue: String): AttributeValue {
    return this.toBuilder().s(stringValue).build();
}