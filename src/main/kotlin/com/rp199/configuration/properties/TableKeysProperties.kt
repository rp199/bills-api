package com.rp199.configuration.properties

import io.quarkus.arc.config.ConfigProperties

@ConfigProperties(prefix = "dynamodb-table.keys")
data class TableKeysProperties(var userNamePrefix: String = "",
                               var billsGroupPrefix: String = "",
                               var yearMonthPrefix: String = "")