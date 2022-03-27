package com.designdrivendevelopment.voicediary

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecordsFragment : Fragment() {
    private var settingsFab: FloatingActionButton? = null
    private var startRecordFab: FloatingActionButton? = null
    private var isRecordPermissionGranted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        requireActivity().title = getString(R.string.title_records_fragment)
        initViews(view)
        setupListeners(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearViews()
    }

    private fun initViews(view: View) {
        settingsFab = view.findViewById(R.id.open_settings_fab)
        startRecordFab = view.findViewById(R.id.start_record_fab)
    }

    private fun clearViews() {
        settingsFab = null
        startRecordFab = null
    }

    private fun setupListeners(context: Context) {
        startRecordFab?.setOnClickListener {
            updatePermissionStatus(context)
            if (!isRecordPermissionGranted) {
                setFragmentResult(REQUEST_RECORD_PERMISSION_KEY, Bundle())
            } else {
//                Start recording
            }
        }
        settingsFab?.setOnClickListener {
            setFragmentResult(OPEN_SETTINGS_KEY, Bundle())
        }
    }

    private fun updatePermissionStatus(context: Context) {
        isRecordPermissionGranted = context
            .getSharedPreferences(SettingsFragment.SETTINGS_PREFS, 0)
            .getBoolean(RECORD_PERMISSION, false)
    }

    companion object {
        const val OPEN_SETTINGS_KEY = "open_settings_fragment"
        const val REQUEST_RECORD_PERMISSION_KEY = "request_record_permission"
        const val RECORD_PERMISSION = "record_permission_granted"

        @JvmStatic
        fun newInstance() = RecordsFragment()
    }
}