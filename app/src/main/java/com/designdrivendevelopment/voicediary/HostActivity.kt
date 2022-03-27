package com.designdrivendevelopment.voicediary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit

class HostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        setupFragmentListener()
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, RecordsFragment.newInstance())
            }
        }
    }

    private fun setupFragmentListener() {
        supportFragmentManager.apply {
            setFragmentResultListener(
                RecordsFragment.OPEN_SETTINGS_KEY, this@HostActivity
            ) { _, _ ->
                this.commit {
                    replace(R.id.fragment_container, SettingsFragment.newInstance())
                    addToBackStack(null)
                }
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