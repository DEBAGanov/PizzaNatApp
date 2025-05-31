/**
 * @file: ApiResult.kt
 * @description: Wrapper для обработки результатов API вызовов
 * @dependencies: Retrofit
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.remote.util

import retrofit2.Response
import java.io.IOException

/**
 * Безопасный вызов API с обработкой ошибок
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            ApiResult.Success(response.body())
        } else {
            val errorMessage = when (response.code()) {
                400 -> "Неверный запрос"
                401 -> "Не авторизован"
                403 -> "Доступ запрещен"
                404 -> "Ресурс не найден"
                409 -> "Конфликт данных"
                500 -> "Ошибка сервера"
                else -> "Неизвестная ошибка: ${response.code()}"
            }
            ApiResult.Error(
                code = response.code(),
                message = errorMessage,
                body = response.errorBody()?.string()
            )
        }
    } catch (exception: IOException) {
        ApiResult.NetworkError("Проблемы с сетью: ${exception.message}")
    } catch (exception: Exception) {
        ApiResult.Error(
            code = -1,
            message = "Неожиданная ошибка: ${exception.message}",
            body = null
        )
    }
}

/**
 * Результат API вызова
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T?) : ApiResult<T>()
    data class Error(val code: Int, val message: String, val body: String?) : ApiResult<Nothing>()
    data class NetworkError(val message: String) : ApiResult<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
    
    val isError: Boolean
        get() = this is Error || this is NetworkError
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getErrorMessage(): String = when (this) {
        is Error -> message
        is NetworkError -> message
        else -> ""
    }
}

/**
 * Преобразование ApiResult в Result
 */
fun <T> ApiResult<T>.toResult(): Result<T> {
    return when (this) {
        is ApiResult.Success -> {
            if (data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception("Пустой ответ от сервера"))
            }
        }
        is ApiResult.Error -> {
            Result.failure(Exception(message))
        }
        is ApiResult.NetworkError -> {
            Result.failure(IOException(message))
        }
    }
} 