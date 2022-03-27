package com.designdrivendevelopment.voicediary

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.vk.api.sdk.VK

class SettingsFragment : Fragment() {
    private var skipAuthCheckBox: CheckBox? = null
    private var authButton: Button? = null
    private var syncButton: Button? = null
    private var prefs: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        requireActivity().title = getString(R.string.title_settings_fragment)
        prefs = requireContext().getSharedPreferences(SETTINGS_PREFS, 0)
        val skipAuth = prefs?.getBoolean(SKIP_AUTH, false) ?: false
        skipAuthCheckBox?.isChecked = skipAuth
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        authButton?.isEnabled = !VK.isLoggedIn()
    }

    override fun onStop() {
        super.onStop()
        prefs?.edit()?.apply {
            putBoolean(SKIP_AUTH, skipAuthCheckBox?.isChecked ?: false)
            apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prefs = null
        clearViews()
    }

    private fun setupListeners() {
        authButton?.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val intent = Intent(context, MainActivity::class.java)
            intent.apply {
                putExtra(MainActivity.LAUNCH_FROM_SETTINGS, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private fun initViews(view: View) {
        skipAuthCheckBox = view.findViewById(R.id.skip_auth_checkbox)
        authButton = view.findViewById(R.id.auth_button)
        syncButton = view.findViewById(R.id.sync_button)
    }

    private fun clearViews() {
        skipAuthCheckBox = null
        authButton = null
        syncButton = null
    }

    companion object {
        const val SETTINGS_PREFS = "SETTINGS"
        const val SKIP_AUTH = "skip_auth"

        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}