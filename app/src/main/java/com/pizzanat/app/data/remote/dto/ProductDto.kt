/**
 * @file: ProductDto.kt
 * @description: DTO классы для работы с продуктами и категориями API
 * @dependencies: Gson
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO для продукта
 */
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
    val categoryName: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("discountPercent")
    val discountPercent: Int? = null,
    @SerializedName("available")
    val available: Boolean = true,
    @SerializedName("specialOffer")
    val specialOffer: Boolean = false
)

/**
 * DTO для категории продуктов
 */
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
    val displayOrder: Int? = null
)

/**
 * DTO для пагинированного ответа продуктов
 */
data class ProductsPageResponse(
    @SerializedName("content")
    val content: List<ProductDto>,
    @SerializedName("pageable")
    val pageable: PageableDto,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("size")
    val size: Int,
    @SerializedName("number")
    val number: Int,
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("empty")
    val empty: Boolean,
    @SerializedName("sort")
    val sort: SortDto? = null
)

/**
 * DTO для информации о пагинации
 */
data class PageableDto(
    @SerializedName("pageNumber")
    val pageNumber: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("offset")
    val offset: Long,
    @SerializedName("paged")
    val paged: Boolean,
    @SerializedName("unpaged")
    val unpaged: Boolean,
    @SerializedName("sort")
    val sort: SortDto? = null
)

/**
 * DTO для информации о сортировке
 */
data class SortDto(
    @SerializedName("empty")
    val empty: Boolean,
    @SerializedName("sorted")
    val sorted: Boolean,
    @SerializedName("unsorted")
    val unsorted: Boolean
)

/**
 * DTO для старой ProductsResponse (оставляем для совместимости)
 */
data class ProductsResponse(
    @SerializedName("content")
    val content: List<ProductDto>,
    @SerializedName("totalElements")
    val totalElements: Long = 0,
    @SerializedName("totalPages") 
    val totalPages: Int = 1,
    @SerializedName("numberOfElements")
    val numberOfElements: Int = 0,
    @SerializedName("first")
    val first: Boolean = true,
    @SerializedName("last")
    val last: Boolean = true
)

data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<CategoryDto>
) 