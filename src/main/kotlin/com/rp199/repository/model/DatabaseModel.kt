package com.rp199.repository.model

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.YearMonth

interface DynamoDbBean<K, S> {
    fun getPkValue(): K
    fun setPkValue(value: K)
    fun getSkValue(): S?
    fun setSkValue(value: S?)
}

@RegisterForReflection
data class UserDynamoDbBean(var userName: String = "", var displayName: String? = null) : DynamoDbBean<String, String> {
    override fun getPkValue(): String = userName
    override fun getSkValue(): String? = null
    override fun setPkValue(value: String) {
        userName = value
    }

    override fun setSkValue(value: String?) {
        //Sk is a place holder value
    }
}

@RegisterForReflection
data class MonthlyBillGroupDbBean(var id: String = "", var description: String = "") : DynamoDbBean<String, String> {
    override fun getPkValue(): String = id

    override fun setPkValue(value: String) {
        id = value
    }

    override fun getSkValue(): String? = null

    override fun setSkValue(value: String?) {
        //Sk is a place holder value
    }
}

@RegisterForReflection
data class UserMonthlyBillsDbBean(var userName: String = "", var monthlyBillGroupId: String = "") : DynamoDbBean<String, String> {
    override fun getPkValue(): String = userName

    override fun setPkValue(value: String) {
        userName = value
    }

    override fun getSkValue(): String = monthlyBillGroupId

    override fun setSkValue(value: String?) {
        monthlyBillGroupId = value!!
    }
}

@RegisterForReflection
data class MonthlyBillDbBean(var monthlyBillGroupId: String = "", var yearMonth: YearMonth = YearMonth.now(), var bills: String = "") : DynamoDbBean<String, String> {

    override fun getPkValue(): String = monthlyBillGroupId

    override fun setPkValue(value: String) {
        monthlyBillGroupId = value
    }

    override fun getSkValue(): String = yearMonth.toString()

    override fun setSkValue(value: String?) {
        yearMonth = YearMonth.parse(value!!)
    }

}