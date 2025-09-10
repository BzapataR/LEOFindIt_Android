package com.example.leofindit

import android.app.Application
import com.example.leofindit.koin.initKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(applicationContext = this)
    }
}