package com.rp199.dsl

import com.rp199.repository.model.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema

@DslMarker
annotation class TableSchemaDslMarker

@TableSchemaDslMarker
class TableAsyncDsl<T : DynamoDbBean<String, String>>(private val clazz: Class<T>, private val enhancedAsyncClient: DynamoDbEnhancedAsyncClient) {
    var name = "dummy"
    var schema: StaticTableSchema<T>? = null

    fun schema(block: TableSchemaDsl<T>.() -> Unit) {
        schema = tableSchema(clazz, block)
    }

    fun build(): DynamoDbAsyncTable<T> = enhancedAsyncClient.table(name, schema)
}

@TableSchemaDslMarker
class TableSchemaDsl<T : DynamoDbBean<String, String>>(private val clazz: Class<T>, private val builder: StaticTableSchema.Builder<T>) {
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
class PrefixedKeyDsl<T : DynamoDbBean<String, String>>(private val keyType: KeyType, private val builder: StaticAttribute.Builder<T, String>) {
    var name = ""
    var prefix = ""
    var prefixSeparator = ""
    private var getter: ((T) -> String?)? = null
    private var setter: ((T, String?) -> Unit)? = null

    fun getter(getter: (T) -> String?) {
        this.getter = getter
    }

    fun setter(setter: (T, String?) -> Unit) {
        this.setter = setter
    }

    fun build(): StaticAttribute<T, String> {
        prefix.takeIf { it.isNotEmpty() }?.let {
            builder.attributeConverter(PrefixedStringKeyConvertor(prefix, prefixSeparator))
        }

        when (keyType) {
            KeyType.PARTITION_KEY -> {
                builder.partitionKey()
                if (getter == null) getter = { it.getPkValue() }
                if (setter == null) setter = { b, v -> b.setPkValue(v!!) }
            }
            KeyType.SORT_KEY -> {
                builder.sortKey()
                if (getter == null) getter = { it.getSkValue() }
                if (setter == null) setter = { b, v -> b.setSkValue(v) }
            }
        }
        return builder.name(name).getter(getter).setter(setter).build()
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

//Cannot be inline due an issue while build on native
fun <T : DynamoDbBean<String, String>> asyncTable(clazz: Class<T>, enhancedAsyncClient: DynamoDbEnhancedAsyncClient, block: TableAsyncDsl<T>.() -> Unit): DynamoDbAsyncTable<T> {
    return TableAsyncDsl(clazz, enhancedAsyncClient).apply(block).build()
}

fun <T : DynamoDbBean<String, String>> tableSchema(clazz: Class<T>, block: TableSchemaDsl<T>.() -> Unit): StaticTableSchema<T> {
    return TableSchemaDsl(clazz, StaticTableSchema.builder(clazz)).apply(block).build()
}

fun <T : DynamoDbBean<String, String>> prefixedKey(keyType: KeyType, clazz: Class<T>, block: PrefixedKeyDsl<T>.() -> Unit): StaticAttribute<T, String> {
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

class StringKeys<T : DynamoDbBean<String, String>>(private val clazz: Class<T>) : ArrayList<StaticAttribute<T, String>>() {
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