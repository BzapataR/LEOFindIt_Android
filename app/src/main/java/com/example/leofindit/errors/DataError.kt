package com.example.leofindit.errors

sealed interface DataError : Error {
    enum class DbError : DataError {
        DISK_FULL,
        UNKNOWN,
    }
    enum class Remote: DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER,
        SERIALIZATION,
        UNKNOWN_ERROR
    }

    enum class ScanningError : DataError {
        MISSING_PERMISSIONS,
        BLUETOOTH_DISABLED,
        ALREADY_SCANNING,
        SCANNER_FAILED,
        DEVICE_NOT_FOUND,
        UNKNOWN_ERROR,
    }
    enum class RepositoryError : DataError {
        DEVICE_NOT_FOUND,
    }
}