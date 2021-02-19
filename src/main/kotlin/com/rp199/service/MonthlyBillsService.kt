package com.rp199.service

import com.rp199.model.MonthlyBillsGroupResponse
import com.rp199.repository.DynamoDBRepository
import com.rp199.repository.TransactionalDispatcher
import com.rp199.repository.model.MonthlyBillGroupDbBean
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MonthlyBillsService(
        private val monthlyBillGroupRepository: DynamoDBRepository<String, String, MonthlyBillGroupDbBean>,
        private val entityLinkService: EntityLinkService, private val transactionalDispatcher: TransactionalDispatcher) {

    suspend fun getMonthlyBillsGroupResponse(monthlyBillsGroupId: String): MonthlyBillsGroupResponse? {
        return getMonthlyBillsGroup(monthlyBillsGroupId)?.let {
            MonthlyBillsGroupResponse(it.description, listOf())
        }
    }

    suspend fun getMonthlyBillsGroup(monthlyBillsGroupId: String): MonthlyBillGroupDbBean? {
        return monthlyBillGroupRepository
                .getItem(MonthlyBillGroupDbBean(monthlyBillsGroupId))
    }

    suspend fun createMonthlyBillsGroup(description: String, userNames: List<String>): String {
        val monthlyBillGroupId = UUID.randomUUID().toString()
        val userLinkageActions = userNames.map {
            entityLinkService.addUserToGroupTransactional(it, monthlyBillGroupId)
        }
        transactionalDispatcher.doWriteWithTransaction(userLinkageActions + monthlyBillGroupRepository.transactionalBuilder {
            addPutItem(it, MonthlyBillGroupDbBean(monthlyBillGroupId, description))
        })

        return monthlyBillGroupId
    }

    suspend fun deleteMonthlyBillsGroup(id: String) {
        monthlyBillGroupRepository.deleteItem(MonthlyBillGroupDbBean((id)))
    }

}