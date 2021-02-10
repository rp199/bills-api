package com.rp199.service

import com.rp199.model.PutUserResult
import com.rp199.model.UserAlreadyExists
import com.rp199.model.UserBills
import com.rp199.model.UserCreated
import javax.inject.Singleton

@Singleton
class UserBillsService {

    val userBills = mutableMapOf<String, UserBills>()

    fun createUser(userName: String): PutUserResult {
        if (userBills.containsKey(userName)) {
            return UserAlreadyExists
        }
        userBills += userName to UserBills(userName)
        return UserCreated(userName)
    }


    fun getUserBills(userName: String): UserBills? {
        return userBills[userName]
    }
}