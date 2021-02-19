package com.rp199.model

import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class UserResponse(val userName: String, val displayName: String, val monthlyBillsOverview: List<MonthlyBillsOverview> = listOf())

data class MonthlyBillsOverview(val id: String, val name: String)

@RegisterForReflection
data class MonthlyBillsGroupResponse(val description: String, val users: List<String>)