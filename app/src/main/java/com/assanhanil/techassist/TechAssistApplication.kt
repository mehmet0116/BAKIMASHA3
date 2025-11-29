package com.assanhanil.techassist

import android.app.Application
import com.assanhanil.techassist.data.local.TechAssistDatabase
import com.assanhanil.techassist.data.repository.BearingRepositoryImpl
import com.assanhanil.techassist.domain.repository.BearingRepository

/**
 * Application class for ASSANHANİL TECH-ASSIST.
 * Initializes the database and provides dependency injection.
 */
class TechAssistApplication : Application() {

    // Database
    val database: TechAssistDatabase by lazy {
        TechAssistDatabase.getDatabase(this)
    }

    // Repositories
    val bearingRepository: BearingRepository by lazy {
        BearingRepositoryImpl(database.bearingDao())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var instance: TechAssistApplication? = null

        fun getInstance(): TechAssistApplication {
            return instance ?: throw IllegalStateException(
                "TechAssistApplication has not been created yet"
            )
        }
    }
}
