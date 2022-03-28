package com.designdrivendevelopment.voicediary

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.commit
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HostActivity : AppCompatActivity() {
    private var recordPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) RecordService.start(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        setupFragmentListeners()
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
                MaterialAlertDialogBuilder(this@HostActivity)
                    .setMessage(R.string.alert_request_record_permission)
                    .setNeutralButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }.setOnDismissListener {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }.show()
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