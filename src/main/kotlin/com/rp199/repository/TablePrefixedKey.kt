package com.rp199.repository

class TablePrefixedKey(private val pkPrefix: String, private val skPrefix: String, private val keySeparator: String) {

    fun getPrefixedPk(pk: String) = "$pkPrefix$keySeparator$pk"

    fun removePrefixFromPK(prefixedPk: String) = prefixedPk.removePrefix("$pkPrefix$keySeparator")

    fun getPrefixedSk(sk: String) = "$skPrefix$keySeparator$sk"

    fun removePrefixFromSk(prefixedSk: String) = prefixedSk.removePrefix("$skPrefix$keySeparator")
}