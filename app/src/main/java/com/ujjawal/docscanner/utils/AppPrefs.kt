package com.ujjawal.docscanner.utils

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {
    private const val NAME = "doc_scanner_prefs"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    // Settings screen
    fun isFocusSoundEnabled(ctx: Context) = prefs(ctx).getBoolean("focus_sound", true)
    fun setFocusSound(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("focus_sound", v).apply()

    fun isCaptureSoundEnabled(ctx: Context) = prefs(ctx).getBoolean("capture_sound", true)
    fun setCaptureSound(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean("capture_sound", v).apply()

    // Color filter: ORIGINAL, GRAYSCALE, BW, ENHANCED
    fun getColorFilter(ctx: Context) = prefs(ctx).getString("color_filter", "ORIGINAL") ?: "ORIGINAL"
    fun setColorFilter(ctx: Context, v: String) = prefs(ctx).edit().putString("color_filter", v).apply()

    // Home menu
    // View mode: list, grid
    fun getViewMode(ctx: Context) = prefs(ctx).getString("view_mode", "list") ?: "list"
    fun setViewMode(ctx: Context, v: String) = prefs(ctx).edit().putString("view_mode", v).apply()

    // Sort: date_added, name, date_modified
    fun getSortBy(ctx: Context) = prefs(ctx).getString("sort_by", "date_added") ?: "date_added"
    fun setSortBy(ctx: Context, v: String) = prefs(ctx).edit().putString("sort_by", v).apply()

    // Default export: pdf, jpeg
    fun getDefaultExport(ctx: Context) = prefs(ctx).getString("default_export", "pdf") ?: "pdf"
    fun setDefaultExport(ctx: Context, v: String) = prefs(ctx).edit().putString("default_export", v).apply()
}
