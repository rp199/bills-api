package com.rp199.dsl

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class PrefixedStringKeyConvertor(keyPrefix: String, prefixSeparator: String? = null) : AttributeConverter<String> {

    private val prefixAndSeparator = "$keyPrefix${prefixSeparator.orEmpty()}"

    override fun transformFrom(input: String?): AttributeValue {
        return AttributeValue.builder().s("${prefixAndSeparator}$input")
                .build()
    }

    override fun transformTo(input: AttributeValue): String {
        return input.s().removePrefix(prefixAndSeparator)
    }

    override fun type(): EnhancedType<String> {
        return EnhancedType.of(String::class.java)
    }

    override fun attributeValueType(): AttributeValueType {
        return AttributeValueType.S
    }
}