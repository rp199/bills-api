package com.rp199.service

import com.rp199.model.CreateUserRequest
import com.rp199.model.PutUserRequest
import com.rp199.model.PutUserResult
import com.rp199.model.UpdateUserRequest
import com.rp199.model.UserAlreadyExists
import com.rp199.model.UserCreated
import com.rp199.model.UserResponse
import com.rp199.repository.DynamoDBRecord
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.model.UserViewModel
import com.rp199.repository.sealed.ItemCreated
import com.rp199.repository.sealed.KeysAlreadyOnTheDatabase
import com.rp199.repository.sealed.PutItemResult
import javax.inject.Singleton

@Singleton
class UserService(private val userDynamoDBView: DynamoDBRepository<UserViewModel>) {

    suspend fun createUser(createUserRequest: CreateUserRequest): PutUserResult {
        return putUser(createUserRequest.userName, createUserRequest.displayName) {
            this.putItemViewIfNotExists(it)
        }
    }

    suspend fun createOrUpdateUser(userName: String, putUserRequest: PutUserRequest): PutUserResult {
        return putUser(userName, putUserRequest.displayName) {
            this.putItemView(it)
        }
    }

    private suspend fun putUser(userName: String, displayName: String?, putAction: suspend DynamoDBRepository<UserViewModel>.(UserViewModel) -> PutItemResult): PutUserResult {
        return when (val putItemResult = userDynamoDBView.putAction(UserViewModel(userName, displayName
                ?: userName))) {
            is ItemCreated -> UserCreated(putItemResult.pk)
            is KeysAlreadyOnTheDatabase -> UserAlreadyExists
        }
    }

    suspend fun getUser(userName: String): UserResponse? {
        return userDynamoDBView.getItemView(userName)?.let {
            UserResponse(it.userName, it.displayName)
        }
    }

    suspend fun deleteUser(userName: String) {
        userDynamoDBView.deleteItemView(userName)
    }

    suspend fun updateUser(userName: String, updateUserRequest: UpdateUserRequest) {
        userDynamoDBView.updateItem(DynamoDBRecord(userName, attributes = mapOf()))
    }

    suspend fun updateUserDisplayName(displayName: String) {

    }

}