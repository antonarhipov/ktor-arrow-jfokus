package com.example.external.impl

import com.example.ProductId
import com.example.external.Warehouse
import io.ktor.http.*

class WarehouseImpl(private val serviceUrl: Url) : Warehouse {
    override suspend fun checkAvailability(productId: ProductId, amount: Int): Boolean {
        TODO("Not yet implemented")
    }
}