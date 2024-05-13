package com.pokhuimand.photoeditor

import android.app.Application
import com.pokhuimand.photoeditor.data.AppContainer

class PhotoEditorApplication : Application() {
    lateinit var appContainer: AppContainer
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this.applicationContext)
    }
}