package com.example.external.impl

import com.example.external.Billing
import com.example.external.BillingResponse
import io.ktor.http.*

class BillingImpl(private val serviceUrl: Url) : Billing {
    override suspend fun processBilling(order: Map<String, Int>): BillingResponse {
        TODO("Not yet implemented")
    }
}