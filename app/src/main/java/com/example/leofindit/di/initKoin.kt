package com.example.leofindit.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null, applicationContext: Context) {
    startKoin {
        config?.invoke(this)
        androidLogger(Level.DEBUG)
        androidContext(applicationContext)
        modules(modules)
    }
}