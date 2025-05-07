package com.example.leofindit.model

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
}