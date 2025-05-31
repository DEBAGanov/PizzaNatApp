/**
 * @file: CategoryDto.kt
 * @description: DTO класс для категории продуктов из API
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("imageUrl")
    val imageUrl: String?
) 
 