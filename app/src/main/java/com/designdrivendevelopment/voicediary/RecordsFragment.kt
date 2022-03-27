package com.designdrivendevelopment.voicediary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecordsFragment : Fragment() {
    private var settingsFab: FloatingActionButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Ваши записи"
        initViews(view)
        settingsFab?.setOnClickListener {
            setFragmentResult(OPEN_SETTINGS_KEY, Bundle())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearViews()
    }

    private fun initViews(view: View) {
        settingsFab = view.findViewById(R.id.open_settings_fab)
    }

    private fun clearViews() {
        settingsFab = null
    }

    companion object {
        const val OPEN_SETTINGS_KEY = "open_settings_fragment"

        @JvmStatic
        fun newInstance() = RecordsFragment()
    }
}