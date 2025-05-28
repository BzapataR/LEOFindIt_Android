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

    @Query("DELETE FROM btle_devices")
    suspend fun deleteAll()

    @Query("SELECT * FROM btle_devices")
    fun getAllDevices(): Flow<List<BTLEDeviceEntity>>

    @Query("SELECT * FROM btle_devices WHERE deviceAddress = :deviceAddress")
    fun getDeviceByAddress(deviceAddress: String): Flow<BTLEDeviceEntity?>

    @Query("Select * FROM btle_devices WHERE `is Suspicious` = False")
    fun getSafeDevices(): Flow<List<BTLEDeviceEntity>>

    @Query("Select * FROM btle_devices WHERE `is Suspicious`= True")
    fun getSusDevices(): Flow<List<BTLEDeviceEntity>>

    @Query("SELECT `is Suspicious` FROM btle_devices WHERE deviceAddress = :deviceAddress")
    fun getDeviceStatus(deviceAddress: String) : Flow<Boolean?>

}