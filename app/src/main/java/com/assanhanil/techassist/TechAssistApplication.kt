package com.assanhanil.techassist

import android.app.Application
import com.assanhanil.techassist.data.local.TechAssistDatabase
import com.assanhanil.techassist.data.preferences.ThemePreferences
import com.assanhanil.techassist.data.repository.BearingRepositoryImpl
import com.assanhanil.techassist.data.repository.ExcelTemplateRepositoryImpl
import com.assanhanil.techassist.data.repository.MachineControlRepositoryImpl
import com.assanhanil.techassist.data.repository.OperatorRepositoryImpl
import com.assanhanil.techassist.data.repository.RecipeRepositoryImpl
import com.assanhanil.techassist.data.repository.ReportRepositoryImpl
import com.assanhanil.techassist.domain.repository.BearingRepository
import com.assanhanil.techassist.domain.repository.ExcelTemplateRepository
import com.assanhanil.techassist.domain.repository.MachineControlRepository
import com.assanhanil.techassist.domain.repository.OperatorRepository
import com.assanhanil.techassist.domain.repository.RecipeRepository
import com.assanhanil.techassist.domain.repository.ReportRepository

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
    
    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(database.recipeDao())
    }
    
    val reportRepository: ReportRepository by lazy {
        ReportRepositoryImpl(database.reportDao())
    }
    
    val excelTemplateRepository: ExcelTemplateRepository by lazy {
        ExcelTemplateRepositoryImpl(database.excelTemplateDao())
    }
    
    val machineControlRepository: MachineControlRepository by lazy {
        MachineControlRepositoryImpl(database.machineControlDao())
    }
    
    val operatorRepository: OperatorRepository by lazy {
        OperatorRepositoryImpl(database.operatorDao())
    }
    
    // Theme Preferences
    val themePreferences: ThemePreferences by lazy {
        ThemePreferences(this)
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
