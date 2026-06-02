package com.scipath.makemegrow.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.databinding.ActivitySettingsBinding
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val app = application as MakeMeGrowApp
        val settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java]

        // General
        // Confirmation of Completion
        settingsViewModel.confirmationOfCompletion.observe(this) { enabled ->
            binding.textConfirmCompleting.setText(
                if (enabled) R.string.enabled
                else R.string.disabled
            )
            if (binding.checkboxConfirmCompleting.isChecked != enabled)
                binding.checkboxConfirmCompleting.isChecked = enabled
        }

        binding.checkboxConfirmCompleting.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setConfirmationOfCompletion(isChecked)
        }

        // About
        binding.textAppName.text = applicationContext.getString(
            R.string.app,
            applicationContext.getString(R.string.app_name)
        )

        binding.textAppVersion.text = applicationContext.getString(
            R.string.version,
            packageInfo.versionName
        )

        // Button Back
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}