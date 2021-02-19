package com.rp199.service

import com.rp199.model.CreateUserRequest
import com.rp199.model.MonthlyBillsOverview
import com.rp199.model.PutUserRequest
import com.rp199.model.PutUserResult
import com.rp199.model.UpdateUserRequest
import com.rp199.model.UserAlreadyExists
import com.rp199.model.UserCreated
import com.rp199.model.UserResponse
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.model.UserDynamoDbBean
import com.rp199.repository.model.UserMonthlyBillsDbBean
import com.rp199.repository.sealed.ItemCreated
import com.rp199.repository.sealed.KeysAlreadyOnTheDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserService(private val userDynamoDbRepository: DynamoDBRepository<String, String, UserDynamoDbBean>,
                  private val userMonthlyBillsRepository: DynamoDBRepository<String, String, UserMonthlyBillsDbBean>,
                  private val monthlyBillsService: MonthlyBillsService
) {

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


    suspend fun getUser(userName: String): UserResponse? = coroutineScope {
        val groupBeans = async {
            userMonthlyBillsRepository.queryKey(userName)
                    .mapNotNull {
                        monthlyBillsService.getMonthlyBillsGroup(it.monthlyBillGroupId)
                    }.map { MonthlyBillsOverview(it.id, it.description) }.toList()
        }

        withContext(Dispatchers.IO) {
            userDynamoDbRepository.getItem(UserDynamoDbBean(userName))?.let {
                UserResponse(it.userName, it.displayName!!, listOf())
            }
        }?.let {
            UserResponse(it.userName, it.displayName, groupBeans.await())
        }
    }

    suspend fun deleteUser(userName: String) {
        userDynamoDbRepository.deleteItem(UserDynamoDbBean(userName))
    }

    suspend fun updateUser(userName: String, updateUserRequest: UpdateUserRequest): UserResponse? {
        return userDynamoDbRepository.updateItem(UserDynamoDbBean(userName, updateUserRequest.displayName))?.let {
            UserResponse(it.userName, it.displayName!!, listOf())
        }
    }
}