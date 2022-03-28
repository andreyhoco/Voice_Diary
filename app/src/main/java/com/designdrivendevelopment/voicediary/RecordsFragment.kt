package com.designdrivendevelopment.voicediary

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RecordsFragment : Fragment() {
    private var recordsList: RecyclerView? = null
    private var settingsFab: FloatingActionButton? = null
    private var startRecordFab: FloatingActionButton? = null
    private var stopRecordFab: FloatingActionButton? = null
    private var isRecordPermissionGranted: Boolean = false
    private var recordService: RecordService? = null
    private var isBound = false
    private var isRecording = false
    private var recordPath = ""
    private var adapter: RecordsAdapter? = null
    private val viewModel: RecordsViewModel by viewModels {
        RecordsViewModelFactory(
            (requireContext().applicationContext as VoiceDiaryApplication)
                .appComponent
                .fileManager
        )
    }

    private val recordServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            isBound = true
            recordService = (binder as? RecordService.LocalBinder)?.getService()
            val recording = recordService?.isRecording ?: false
            viewModel.setRecordingStatus(recording)
            viewModel.updateRecordsList()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
            recordService = null
            viewModel.setRecordingStatus(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRecording = savedInstanceState?.getBoolean(PREVIOUS_STATE) ?: false
        updatePermissionStatus(requireContext())
        setupViewModel(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        if (isRecordPermissionGranted) RecordService.start(context)
        else viewModel.updateRecordsList()

        requireActivity().title = getString(R.string.title_records_fragment)
        initViews(view)
        setupListeners()
        setupFragmentResultListener()
        setFabsStateTo(isRecording)

        val recordsAdapter = RecordsAdapter(context)
        adapter = recordsAdapter
        recordsList?.adapter = recordsAdapter
        recordsList?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val marginItemDecoration = MarginItemDecoration(
            marginVertical = 10,
            marginHorizontal = 12,
            marginBottom = getDisplayHeight(context) / DISPLAY_PARTS_NUMBER
        )
        recordsList?.addItemDecoration(marginItemDecoration)
    }

    override fun onStart() {
        super.onStart()
        if (isRecordPermissionGranted) {
            val context = requireActivity()
            val intent = Intent(context, RecordService::class.java)
            context.bindService(intent, recordServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        val context = requireActivity()
        if (updatePermissionStatus(context)) {
            val intent = Intent(context, RecordService::class.java)
            RecordService.start(context)
            context.bindService(intent, recordServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRecordPermissionGranted && isBound) {
            requireActivity().unbindService(recordServiceConnection)
            isBound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        clearViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(PREVIOUS_STATE, isRecording)
    }

    private fun initViews(view: View) {
        recordsList = view.findViewById(R.id.records_list)
        settingsFab = view.findViewById(R.id.open_settings_fab)
        startRecordFab = view.findViewById(R.id.start_record_fab)
        stopRecordFab = view.findViewById(R.id.stop_record_fab)
    }

    private fun clearViews() {
        recordsList = null
        settingsFab = null
        startRecordFab = null
        stopRecordFab = null
    }

    private fun setupListeners() {
        startRecordFab?.setOnClickListener {
            val context = context ?: return@setOnClickListener
            updatePermissionStatus(context)
            if (!isRecordPermissionGranted) {
                setFragmentResult(REQUEST_RECORD_PERMISSION_KEY, Bundle())
            } else {
                viewModel.updateNewRecordPath()
                if (!isRecording) recordService?.startRecord(recordPath)
                val recording = recordService?.isRecording ?: false
                viewModel.setRecordingStatus(recording)
            }
        }

        stopRecordFab?.setOnClickListener {
            if (isRecording) recordService?.stopRecord()
            val recording = recordService?.isRecording ?: false
            viewModel.setRecordingStatus(recording)
            viewModel.updateRecordsList()
            showBottomSheet()
        }

        settingsFab?.setOnClickListener {
            val context = context ?: return@setOnClickListener
            if (isRecording) {
                showSettingsDialog(context) {
                    recordService?.stopRecord()
                    val recording = recordService?.isRecording ?: false
                    viewModel.setRecordingStatus(recording)
                    setFragmentResult(OPEN_SETTINGS_KEY, Bundle())
                }
            } else setFragmentResult(OPEN_SETTINGS_KEY, Bundle())
        }
    }

    private fun setupViewModel(viewModel : RecordsViewModel) {
        viewModel.apply {
            isRecording.observe(this@RecordsFragment) { newState ->
                if (this@RecordsFragment.isRecording != newState) updateFabsStateTo(newState)
                else setFabsStateTo(newState)
                this@RecordsFragment.isRecording = newState
            }
            records.observe(this@RecordsFragment) { records -> adapter?.submitList(records) }
            newRecordPath.observe(this@RecordsFragment) { path -> recordPath = path }
        }
    }

    private fun setupFragmentResultListener() {
        childFragmentManager.setFragmentResultListener(
            SaveRecordFileNameBottomSheet.RESULT_ENTER_FILENAME_KEY,
            this
        ) { _, bundle ->
            val oldName = bundle.getString(SaveRecordFileNameBottomSheet.OLD_FILENAME).orEmpty()
            val enteredName = bundle.getString(SaveRecordFileNameBottomSheet.ENTERED_FILENAME).orEmpty()
            if (enteredName.isNotEmpty()) {
                if (oldName.isNotEmpty()) viewModel.renameRecord(oldName, enteredName)
                else viewModel.saveCurrRecordAs(enteredName)
            }
        }
    }

    private fun showBottomSheet() {
        SaveRecordFileNameBottomSheet()
            .show(childFragmentManager, SaveRecordFileNameBottomSheet.SAVE_RECORD_FILENAME_TAG)
    }

    private fun showSettingsDialog(context: Context, onPositive: (() -> Unit)? = null) {
        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.alert_open_settings_while_record)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onPositive?.invoke()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateFabsStateTo(newStateIsRecording: Boolean) {
        if (newStateIsRecording) {
            animateEditableTransition(startRecordFab, stopRecordFab)
        } else {
            animateEditableTransition(stopRecordFab, startRecordFab)
        }
    }

    private fun setFabsStateTo(isRecording: Boolean) {
        startRecordFab?.isVisible = !isRecording
        stopRecordFab?.isVisible = isRecording
    }

    private fun updatePermissionStatus(context: Context): Boolean {
        val prevValue = isRecordPermissionGranted
        isRecordPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return !prevValue && isRecordPermissionGranted
    }

    private fun getDisplayHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            windowManager.defaultDisplay.height
        } else {
            windowManager.currentWindowMetrics.bounds.height()
        }
    }

    private fun animateEditableTransition(hiddenView: View?, shownView: View?) {
        val showAnimation = ObjectAnimator.ofFloat(
            shownView,
            View.ROTATION,
            ROTATION_TRANSITION_ANGLE,
            ROTATION_END_ANGLE
        ).apply {
            duration = CHANGE_STATE_ANIMATION_DURATION
        }
        ObjectAnimator.ofFloat(
            hiddenView,
            View.ROTATION,
            ROTATION_START_ANGLE,
            ROTATION_TRANSITION_ANGLE
        ).apply {
            duration = CHANGE_STATE_ANIMATION_DURATION
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        hiddenView?.isVisible = false
                        shownView?.isVisible = true
                        showAnimation.start()
                    }
                }
            )
            start()
        }
    }

    companion object {
        private const val ROTATION_START_ANGLE = 0f
        private const val ROTATION_TRANSITION_ANGLE = 180f
        private const val ROTATION_END_ANGLE = 360f
        private const val CHANGE_STATE_ANIMATION_DURATION = 100L
        private const val PREVIOUS_STATE = "previous_state"
        private const val DISPLAY_PARTS_NUMBER = 4
        const val OPEN_SETTINGS_KEY = "open_settings_fragment"
        const val REQUEST_RECORD_PERMISSION_KEY = "request_record_permission"

        @JvmStatic
        fun newInstance() = RecordsFragment()
    }
}