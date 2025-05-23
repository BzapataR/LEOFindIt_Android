package com.example.leofindit.deviceScanner.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BTLEDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: BTLEDeviceEntity)

    @Update
    suspend fun update(device: BTLEDeviceEntity)

    @Upsert
    suspend fun upsert(device: BTLEDeviceEntity)

    @Delete
    suspend fun delete(device: BTLEDeviceEntity)

    @Query("DELETE FROM btle_devices WHERE deviceAddress = :deviceAddress")
    suspend fun deleteDeviceByAddress(deviceAddress: String)

    @Query("DELETE * FROM btle_devices")
    suspend fun deleteAll()

    @Query("SELECT * FROM btle_devices")
    suspend fun getAllDevices(): Flow<List<BTLEDeviceEntity>>

    @Query("SELECT * FROM btle_devices WHERE deviceAddress = :deviceAddress")
    suspend fun getDeviceByAddress(deviceAddress: String): BTLEDeviceEntity?

    @Query("Select * FROM btle_devices WHERE `is Suspicious` = False")
    suspend fun getSafeDevices(): Flow<List<BTLEDeviceEntity>>

    @Query("Select * FROM btle_devices WHERE `is Suspicious`= True")
    suspend fun getSusDevices(): Flow<List<BTLEDeviceEntity>>
}