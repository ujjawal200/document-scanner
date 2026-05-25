package com.ujjawal.docscanner.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ujjawal.docscanner.R
import com.ujjawal.docscanner.databinding.ActivitySettingsBinding
import com.ujjawal.docscanner.utils.AppPrefs

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Load current values
        binding.switchFocusSound.isChecked = AppPrefs.isFocusSoundEnabled(this)
        binding.switchCaptureSound.isChecked = AppPrefs.isCaptureSoundEnabled(this)

        val currentFilter = AppPrefs.getColorFilter(this)
        binding.toggleColorFilter.check(when (currentFilter) {
            "GRAYSCALE" -> R.id.btnFilterGray
            "BW" -> R.id.btnFilterBW
            "ENHANCED" -> R.id.btnFilterEnhanced
            else -> R.id.btnFilterOriginal
        })

        // Listeners
        binding.switchFocusSound.setOnCheckedChangeListener { _, checked ->
            AppPrefs.setFocusSound(this, checked)
        }
        binding.switchCaptureSound.setOnCheckedChangeListener { _, checked ->
            AppPrefs.setCaptureSound(this, checked)
        }
        binding.toggleColorFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val filter = when (checkedId) {
                    R.id.btnFilterGray -> "GRAYSCALE"
                    R.id.btnFilterBW -> "BW"
                    R.id.btnFilterEnhanced -> "ENHANCED"
                    else -> "ORIGINAL"
                }
                AppPrefs.setColorFilter(this, filter)
            }
        }
    }
}
