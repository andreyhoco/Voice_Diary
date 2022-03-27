package com.designdrivendevelopment.voicediary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit

class HostActivity : AppCompatActivity() {
    private var recordPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val prefs = getSharedPreferences(SettingsFragment.SETTINGS_PREFS, MODE_PRIVATE)
                prefs.edit()?.apply {
                    putBoolean(RecordsFragment.RECORD_PERMISSION, isGranted)
                    apply()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        setupFragmentListeners()
        val isRecordPermissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        val prefs = getSharedPreferences(SettingsFragment.SETTINGS_PREFS, MODE_PRIVATE)
        prefs.edit()?.apply {
            putBoolean(RecordsFragment.RECORD_PERMISSION, isRecordPermissionGranted)
            apply()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, RecordsFragment.newInstance())
            }
        }
    }

    private fun setupFragmentListeners() {
        supportFragmentManager.apply {
            setFragmentResultListener(
                RecordsFragment.OPEN_SETTINGS_KEY, this@HostActivity
            ) { _, _ ->
                this.commit {
                    replace(R.id.fragment_container, SettingsFragment.newInstance())
                    addToBackStack(null)
                }
            }
            setFragmentResultListener(
                RecordsFragment.REQUEST_RECORD_PERMISSION_KEY, this@HostActivity
            ) { _, _ ->
                // Показать диалог
                recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, HostActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}