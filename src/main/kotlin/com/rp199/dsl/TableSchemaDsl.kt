package com.rp199.dsl

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema

@DslMarker
annotation class TableSchemaDslMarker

@TableSchemaDslMarker
class TableAsyncDsl<T>(private val clazz: Class<T>, private val enhancedAsyncClient: DynamoDbEnhancedAsyncClient) {
    var name = "dummy"
    var schema: StaticTableSchema<T>? = null

    fun schema(block: TableSchemaDsl<T>.() -> Unit) {
        schema = tableSchema(clazz, block)
    }

    fun build(): DynamoDbAsyncTable<T> = enhancedAsyncClient.table(name, schema)
}

@TableSchemaDslMarker
class TableSchemaDsl<T>(private val clazz: Class<T>, private val builder: StaticTableSchema.Builder<T>) {
    var name = ""
    private val keys = mutableListOf<StaticAttribute<T, *>>()
    private val attributes = mutableListOf<StaticAttribute<T, *>>()

    fun keys(block: StringKeys<T>.() -> Unit) {
        keys.addAll(StringKeys(clazz).apply(block))
    }

    fun attributes(block: Attributes<T>.() -> Unit) {
        attributes.addAll(Attributes(clazz).apply(block))
    }

    fun build(): StaticTableSchema<T> {
        keys.forEach {
            builder.addAttribute(it)
        }
        attributes.forEach {
            builder.addAttribute(it)
        }

        return builder.newItemSupplier { clazz.getDeclaredConstructor().newInstance() }.build()
    }
}

@TableSchemaDslMarker
class PrefixedKeyDsl<T>(private val keyType: KeyType, private val builder: StaticAttribute.Builder<T, String>) {
    var name = ""
    var prefix = ""
    var prefixSeparator = ""

    fun getter(getter: (T) -> String?) {
        builder.getter(getter)
    }

    fun setter(setter: (T, String?) -> Unit) {
        builder.setter(setter)
    }

    fun build(): StaticAttribute<T, String> {
        prefix.takeIf { it.isNotEmpty() }?.let {
            builder.attributeConverter(PrefixedStringKeyConvertor(prefix, prefixSeparator))
        }

        when (keyType) {
            KeyType.PARTITION_KEY -> builder.partitionKey()
            KeyType.SORT_KEY -> builder.sortKey()
        }
        return builder.name(name).build()
    }
}

@TableSchemaDslMarker
class AttributeDsl<T, S>(private val builder: StaticAttribute.Builder<T, S>) {
    var name = ""

    fun getter(getter: (T) -> S?) {
        builder.getter(getter)
    }

    fun setter(setter: (T, S?) -> Unit) {
        builder.setter(setter)
    }

    fun build(): StaticAttribute<T, S> {
        return builder.name(name).build()
    }
}

inline fun <reified T> asyncTable(enhancedAsyncClient: DynamoDbEnhancedAsyncClient, block: TableAsyncDsl<T>.() -> Unit): DynamoDbAsyncTable<T> {
    return TableAsyncDsl(T::class.java, enhancedAsyncClient).apply(block).build()
}

fun <T> tableSchema(clazz: Class<T>, block: TableSchemaDsl<T>.() -> Unit): StaticTableSchema<T> {
    return TableSchemaDsl(clazz, StaticTableSchema.builder(clazz)).apply(block).build()
}

fun <T> prefixedKey(keyType: KeyType, clazz: Class<T>, block: PrefixedKeyDsl<T>.() -> Unit): StaticAttribute<T, String> {
    return PrefixedKeyDsl(keyType, StaticAttribute.builder(clazz, String::class.java)).apply(block).build()
}

inline fun <T, reified S> attributeBuilder(clazz: Class<T>, block: AttributeDsl<T, S>.() -> Unit): StaticAttribute<T, S> {
    return AttributeDsl(StaticAttribute.builder(clazz, S::class.java)).apply(block).build()
}

class Attributes<T>(val clazz: Class<T>) : ArrayList<StaticAttribute<T, *>>() {
    inline fun <reified S> attribute(attributeBuilder: AttributeDsl<T, S>.() -> Unit) {
        add(attributeBuilder(clazz, attributeBuilder))
    }
}

class StringKeys<T>(private val clazz: Class<T>) : ArrayList<StaticAttribute<T, String>>() {
    fun key(keyType: KeyType, keyBuilder: PrefixedKeyDsl<T>.() -> Unit) {
        add(prefixedKey(keyType, clazz, keyBuilder))
    }
}

fun <T, S> StaticAttribute.Builder<T, S>.partitionKey(): StaticAttribute.Builder<T, S> {
    return addTag(StaticAttributeTags.primaryPartitionKey())
}

fun <T, S> StaticAttribute.Builder<T, S>.sortKey(): StaticAttribute.Builder<T, S> {
    return addTag(StaticAttributeTags.primarySortKey())
}

enum class KeyType {
    PARTITION_KEY, SORT_KEY
}