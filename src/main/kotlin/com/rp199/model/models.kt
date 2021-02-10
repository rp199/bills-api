package com.rp199.model

import java.math.BigDecimal
import java.time.YearMonth

data class MonthlyBill(val id: String, val name: String, val billByMonth: Map<YearMonth, List<Bill>>)

data class Bill(val id: String, val name: String, val totalAmount: BigDecimal, val dueAmount: BigDecimal)

data class UserBills(val userName: String, val monthlyBills: List<MonthlyBill> = mutableListOf())