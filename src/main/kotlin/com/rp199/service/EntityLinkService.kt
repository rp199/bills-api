package com.rp199.service

import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.model.UserMonthlyBillsDbBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class EntityLinkService(private val userMonthlyBillsRepository: DynamoDBRepository<String, String, UserMonthlyBillsDbBean>) {

    fun getMonthlyGroupIds(userName: String): Flow<String> {
        return userMonthlyBillsRepository.queryKey(userName).map { it.monthlyBillGroupId }
    }

    suspend fun addUserToGroup(userName: String, monthlyBillGroupId: String) {
        userMonthlyBillsRepository.putItem(UserMonthlyBillsDbBean(monthlyBillGroupId, userName))
    }

    fun addUserToGroupTransactional(userName: String, monthlyBillGroupId: String): (TransactWriteItemsEnhancedRequest.Builder) -> Unit = userMonthlyBillsRepository.transactionalBuilder {
        addPutItem(it, UserMonthlyBillsDbBean(userName, monthlyBillGroupId))
    }

    fun destroyLink(userName: String, monthlyBillGroupId: String) {

    }
}