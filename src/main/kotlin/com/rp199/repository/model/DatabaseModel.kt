package com.rp199.repository.model

import io.quarkus.runtime.annotations.RegisterForReflection

interface DynamoDbBean<K, S> {
    fun getPkValue(): K?
    fun setPkValue(value: K?)
    fun getSkValue(): S?
    fun setSkValue(value: S?)
}

@RegisterForReflection
data class UserDynamoDbBean(var userName: String? = null, var displayName: String? = null) : DynamoDbBean<String, String> {
    override fun getPkValue(): String? = userName
    override fun getSkValue(): String? = null
    override fun setPkValue(value: String?) {
        userName = value
    }

    override fun setSkValue(value: String?) {
        //Sk is a place holder value
    }
}