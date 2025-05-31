/**
 * @file: ProductDto.kt
 * @description: DTO модели для API продуктов
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("discountedPrice")
    val discountedPrice: Double? = null,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("categoryName")
    val categoryName: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("discountPercent")
    val discountPercent: Int? = null,
    @SerializedName("available")
    val available: Boolean = true,
    @SerializedName("specialOffer")
    val specialOffer: Boolean = false,
    @SerializedName("ingredients")
    val ingredients: String? = null,
    @SerializedName("calories")
    val calories: Int? = null,
    @SerializedName("preparationTime")
    val preparationTime: Int? = null
)

data class CategoryDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("displayOrder")
    val displayOrder: Int
)

data class ProductsResponse(
    @SerializedName("content")
    val content: List<ProductDto>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("number")
    val currentPage: Int,
    @SerializedName("totalElements")
    val totalElements: Int,
    @SerializedName("last")
    val isLast: Boolean,
    @SerializedName("first")
    val isFirst: Boolean,
    @SerializedName("size")
    val size: Int,
    @SerializedName("numberOfElements")
    val numberOfElements: Int
)

data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<CategoryDto>
) 