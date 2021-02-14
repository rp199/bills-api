package com.rp199.service

import com.rp199.model.CreateUserRequest
import com.rp199.model.PutUserRequest
import com.rp199.model.PutUserResult
import com.rp199.model.UpdateUserRequest
import com.rp199.model.UserAlreadyExists
import com.rp199.model.UserCreated
import com.rp199.model.UserResponse
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.model.UserDynamoDbBean
import com.rp199.repository.sealed.ItemCreated
import com.rp199.repository.sealed.KeysAlreadyOnTheDatabase
import javax.inject.Singleton

@Singleton
class UserService(private val userDynamoDbRepository: DynamoDBRepository<String, String, UserDynamoDbBean>) {

    suspend fun createUser(createUserRequest: CreateUserRequest): PutUserResult {
        return when (userDynamoDbRepository.putItemIfNotExists(UserDynamoDbBean(createUserRequest.userName, createUserRequest.displayName))) {
            is KeysAlreadyOnTheDatabase -> UserAlreadyExists
            is ItemCreated -> UserCreated(createUserRequest.userName)
        }
    }

    suspend fun createOrUpdateUser(userName: String, putUserRequest: PutUserRequest): PutUserResult {
        userDynamoDbRepository.putItem(UserDynamoDbBean(userName, putUserRequest.displayName))
        return UserCreated(userName)
    }


    suspend fun getUser(userName: String): UserResponse? {
        return userDynamoDbRepository.getItem(UserDynamoDbBean(userName))?.let {
            UserResponse(it.userName!!, it.displayName!!, listOf())
        }
    }

    suspend fun deleteUser(userName: String) {
        userDynamoDbRepository.deleteItem(UserDynamoDbBean(userName))
    }

    suspend fun updateUser(userName: String, updateUserRequest: UpdateUserRequest): UserResponse? {
        return userDynamoDbRepository.updateItem(UserDynamoDbBean(userName, updateUserRequest.displayName))?.let {
            UserResponse(it.userName!!, it.displayName!!, listOf())
        }
    }
}