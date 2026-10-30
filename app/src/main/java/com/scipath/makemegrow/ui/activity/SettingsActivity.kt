package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.converter.JsonConverter
import com.scipath.makemegrow.data.handler.ExceptionHandlerToast
import com.scipath.makemegrow.data.notification.TaskNotificationManager
import com.scipath.makemegrow.data.notification.TaskNotificationManager.Companion.CHANNEL_ID
import com.scipath.makemegrow.databinding.ActivitySettingsBinding
import com.scipath.makemegrow.ui.dialog.FirstDayOfWeekDialog
import com.scipath.makemegrow.ui.dialog.TimeFormatDialog
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class SettingsActivity : AppCompatActivity() {

    private lateinit var taskNotificationManager: TaskNotificationManager
    private lateinit var jsonConverter: JsonConverter
    private lateinit var binding: ActivitySettingsBinding

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@registerForActivityResult

        lifecycleScope.launch {
            try {
                val json = jsonConverter.toJson()
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
            } catch (exception: Exception) {
                ExceptionHandlerToast.handle(exception, this@SettingsActivity)
            }
        }
    }

    val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult

        lifecycleScope.launch {
            try {
                val json = contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: error("Could not read the file")
                jsonConverter.fromJson(json)
            } catch (exception: Exception) {
                ExceptionHandlerToast.handle(exception, this@SettingsActivity)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val app = application as MakeMeGrowApp
        val settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java]
        taskNotificationManager = app.notificationManager
        jsonConverter = JsonConverter(app.taskRepository, app.categoryRepository)

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
                if (timeFormat24) R.string.time_format_24_option
                else R.string.time_format_12_option
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

        // Notifications
        // Deadline Notifications
        settingsViewModel.deadlineNotifications.observe(this) { enabled ->
            binding.textDeadlineNotifications.setText(
                if (enabled) R.string.enabled
                else R.string.disabled
            )
            binding.checkboxDeadlineNotifications.isChecked = enabled
        }

        binding.checkboxDeadlineNotifications.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setDeadlineNotifications(isChecked)
        }

        // Notifications Sound and Vibration
        binding.containerNotificationsSound.setOnClickListener {
            openNotificationSettings()
        }

        binding.containerNotificationsVibration.setOnClickListener {
            openNotificationSettings()
        }

        // Export/Import
        binding.buttonExport.setOnClickListener {
            val dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            exportLauncher.launch("MakeMeGrow_$dateTime.json")
        }

        binding.buttonImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "text/plain"))
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

    override fun onResume() {
        super.onResume()

        // Notifications Sound and Vibration
        val soundEnabled = taskNotificationManager.isNotificationSoundEnabled()
        val vibrationEnabled = taskNotificationManager.isNotificationVibrationEnabled()
        updateUI(soundEnabled, vibrationEnabled)
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun updateUI(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        binding.textNotificationsSound.setText(
            if (soundEnabled) R.string.enabled
            else R.string.disabled
        )

        binding.textNotificationsVibration.setText(
            if (vibrationEnabled) R.string.enabled
            else R.string.disabled
        )
    }
}