package com.example.leofindit.errors

sealed interface DataError : Error {
    enum class DbError : DataError {
        DISK_FULL,
        UNKNOWN,
    }

    enum class ScanningError : DataError {
        MISSING_PERMISSIONS,
        BLUETOOTH_DISABLED,
        ALREADY_SCANNING,
        SCANNER_FAILED,
        UNKNOWN_ERROR,
    }
}