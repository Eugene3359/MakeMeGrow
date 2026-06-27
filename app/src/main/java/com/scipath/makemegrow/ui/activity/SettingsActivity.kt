package com.scipath.makemegrow.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.databinding.ActivitySettingsBinding
import com.scipath.makemegrow.ui.dialog.FirstDayOfWeekDialog
import com.scipath.makemegrow.ui.dialog.TimeFormatDialog
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale


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
            binding.checkboxConfirmCompleting.isChecked = enabled
        }

        binding.checkboxConfirmCompleting.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setConfirmationOfCompletion(isChecked)
        }

        // First Day of Week
        settingsViewModel.firstDayOfWeek.observe(this) { dayOfWeek ->
            binding.textDayOfWeek.text = dayOfWeek.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )
        }

        binding.containerFirstDayOfWeek.setOnClickListener {
            FirstDayOfWeekDialog
                .newInstance(settingsViewModel.getFirstDayOfWeek())
                .show(supportFragmentManager, "FirstDayOfWeekDialog")
        }

        supportFragmentManager.setFragmentResultListener(
            FirstDayOfWeekDialog.REQUEST_KEY, this
        ) { _, bundle ->
            val dayOfWeek = DayOfWeek.of(
                bundle.getInt(FirstDayOfWeekDialog.RESULT_KEY)
            )
            settingsViewModel.setFirstDayOfWeek(dayOfWeek)
        }

        // Time Format
        settingsViewModel.timeFormat24.observe(this) { timeFormat24 ->
            binding.textTimeFormat.setText(
                if (timeFormat24) R.string.twenty_four_hour_format
                else R.string.twelve_hour_format
            )
        }

        binding.containerTimeFormat.setOnClickListener {
            TimeFormatDialog
                .newInstance(settingsViewModel.isTimeFormat24())
                .show(supportFragmentManager, "TimeFormatDialog")
        }

        supportFragmentManager.setFragmentResultListener(
            TimeFormatDialog.REQUEST_KEY, this
        ) { _, bundle ->
            val timeFormat24 = bundle.getBoolean(TimeFormatDialog.RESULT_KEY)
            settingsViewModel.setTimeFormat24(timeFormat24)
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