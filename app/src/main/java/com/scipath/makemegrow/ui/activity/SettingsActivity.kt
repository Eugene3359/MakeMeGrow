package com.scipath.makemegrow.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.scipath.makemegrow.R
import com.scipath.makemegrow.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)

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