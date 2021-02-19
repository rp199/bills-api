package com.rp199.configuration.properties

import io.quarkus.arc.config.ConfigProperties

@ConfigProperties(prefix = "dynamodb-table.definitions")
data class DynamoDBTableDefinitionsProperties(var tableName: String = "",
                                              var hashKey: String = "",
                                              var sortKey: String = "",
                                              var sortKeyPlaceHolder: String? = "",
                                              var keySeparator: String = "")