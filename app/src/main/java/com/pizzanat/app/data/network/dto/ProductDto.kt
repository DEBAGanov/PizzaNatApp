/**
 * @file: ProductDto.kt
 * @description: DTO класс для продукта из API
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("imageUrl")
    val imageUrl: String?,
    
    @SerializedName("categoryId")
    val categoryId: Long,
    
    @SerializedName("available")
    val available: Boolean = true
) 