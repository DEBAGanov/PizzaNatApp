/**
 * @file: AdminUser.kt
 * @description: Доменная сущность администратора системы
 * @dependencies: Enum классы для ролей и разрешений
 * @created: 2024-12-19
 */
package com.pizzanat.app.domain.entities

import java.time.LocalDateTime

enum class AdminRole(val displayName: String) {
    SUPER_ADMIN("Супер администратор"),
    MANAGER("Менеджер"),
    OPERATOR("Оператор")
}

enum class AdminPermission {
    MANAGE_ORDERS,      // Управление заказами
    MANAGE_PRODUCTS,    // Управление продуктами
    MANAGE_CATEGORIES,  // Управление категориями
    MANAGE_USERS,       // Управление пользователями
    VIEW_ANALYTICS,     // Просмотр аналитики
    MANAGE_ADMINS       // Управление администраторами
}

data class AdminUser(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: AdminRole,
    val permissions: Set<AdminPermission>,
    val isActive: Boolean,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime
) {
    val fullName: String
        get() = "$firstName $lastName"
        
    fun hasPermission(permission: AdminPermission): Boolean {
        return permissions.contains(permission)
    }
    
    fun canManageOrders(): Boolean = hasPermission(AdminPermission.MANAGE_ORDERS)
    fun canManageProducts(): Boolean = hasPermission(AdminPermission.MANAGE_PRODUCTS)
    fun canViewAnalytics(): Boolean = hasPermission(AdminPermission.VIEW_ANALYTICS)
    
    companion object {
        fun getPermissionsForRole(role: AdminRole): Set<AdminPermission> {
            return when (role) {
                AdminRole.SUPER_ADMIN -> AdminPermission.values().toSet()
                AdminRole.MANAGER -> setOf(
                    AdminPermission.MANAGE_ORDERS,
                    AdminPermission.MANAGE_PRODUCTS,
                    AdminPermission.MANAGE_CATEGORIES,
                    AdminPermission.VIEW_ANALYTICS
                )
                AdminRole.OPERATOR -> setOf(
                    AdminPermission.MANAGE_ORDERS,
                    AdminPermission.VIEW_ANALYTICS
                )
            }
        }
    }
} 