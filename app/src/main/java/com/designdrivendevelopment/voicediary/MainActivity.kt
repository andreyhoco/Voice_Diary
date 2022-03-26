package com.designdrivendevelopment.voicediary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException

class MainActivity : AppCompatActivity() {
    private var authLauncher: ActivityResultLauncher<Collection<VKScope>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (VK.isLoggedIn()) {
            HostActivity.start(this@MainActivity)
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        supportActionBar?.elevation = 0f

        authLauncher = VK.login(this) { result : VKAuthenticationResult ->
            when (result) {
                is VKAuthenticationResult.Success -> onLogin()
                is VKAuthenticationResult.Failed -> onLoginFailed(result.exception)
            }
        }

        val authButton = findViewById<Button>(R.id.auth_button)
        authButton.setOnClickListener {
            authLauncher?.launch(arrayListOf(VKScope.DOCS))
        }
    }

    private fun onLogin() {
        HostActivity.start(this@MainActivity)
        finish()
    }

    private fun onLoginFailed(exception: VKAuthException) {
        if (!exception.isCanceled) {
            val descriptionResource =
                if (exception.webViewError == WebViewClient.ERROR_HOST_LOOKUP) R.string.message_connection_error
                else R.string.message_unknown_error
            Snackbar.make(findViewById(android.R.id.content), descriptionResource, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.text_action_retry)) {
                    authLauncher?.launch(arrayListOf(VKScope.DOCS))
                }.show()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}