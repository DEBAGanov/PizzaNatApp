/**
 * @file: MapConverter.kt
 * @description: Room TypeConverter для преобразования Map в JSON и обратно
 * @dependencies: Gson, Room
 * @created: 2024-12-19
 */
package com.pizzanat.app.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
    
    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
} 