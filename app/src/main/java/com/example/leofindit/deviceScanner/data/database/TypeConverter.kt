package com.example.leofindit.deviceScanner.data.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestampList(value: List<Long>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toTimestampList(value: String?): List<Long>? {
        return value?.split(",")?.mapNotNull { it.toLongOrNull() }
    }
    @TypeConverter
    fun fromUUIDList(value: List<String>?): String? {
        return value?.joinToString(",")
    }
    @TypeConverter
    fun toUUIDList(value: String?): List<String>? {
        return value?.split(",")
    }
}