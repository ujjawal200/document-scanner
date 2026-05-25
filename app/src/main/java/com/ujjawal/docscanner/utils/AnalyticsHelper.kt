package com.ujjawal.docscanner.utils

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

object AnalyticsHelper {

    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    fun logScanCompleted(source: String, pageCount: Int) {
        analytics?.logEvent("scan_completed") {
            param("source", source)
            param("page_count", pageCount.toLong())
        }
    }

    fun logFilterApplied(filterType: String) {
        analytics?.logEvent("filter_applied") {
            param("filter_type", filterType)
        }
    }

    fun logOcrUsed(textLength: Int) {
        analytics?.logEvent("ocr_used") {
            param("text_length", textLength.toLong())
        }
    }

    fun logExportCompleted(format: String, pageCount: Int) {
        analytics?.logEvent("export_completed") {
            param("format", format)
            param("page_count", pageCount.toLong())
        }
    }

    fun logDocumentShared(targetApp: String) {
        analytics?.logEvent("document_shared") {
            param("target_app", targetApp)
        }
    }

    fun logCropManual() {
        analytics?.logEvent("crop_manual", null)
    }

    fun logSettingsChanged(settingName: String, value: String) {
        analytics?.logEvent("settings_changed") {
            param("setting_name", settingName)
            param("value", value)
        }
    }

    fun logScreenView(screenName: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }
}
