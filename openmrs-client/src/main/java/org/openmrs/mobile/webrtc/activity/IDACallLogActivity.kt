package org.openmrs.mobile.webrtc.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.openmrs.mobile.webrtc.adapter.CallLogAdapter
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.call.ui.activity.CoreCallLogActivity
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.klivekit.model.RtcArgs
import org.openmrs.mobile.R
import org.openmrs.mobile.databinding.ActivityCallLogBinding


class IDACallLogActivity : CoreCallLogActivity(), BaseViewHolder.ViewHolderClickListener {

    private lateinit var binding: ActivityCallLogBinding
    private lateinit var adapter: CallLogAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCallLogBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        adapter = CallLogAdapter(this, arrayListOf())
        adapter.clickListener = this
    }

    override fun onLogs(logs: List<RtcCallLog>) {
        binding.callLogContent.tvCallLogEmptyMessage.isVisible = false
        binding.callLogContent.rvCallLogs.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    this@IDACallLogActivity, DividerItemDecoration.VERTICAL
                )
            )
            this@IDACallLogActivity.adapter.updateItems(logs.toMutableList())
            adapter = this@IDACallLogActivity.adapter
        }
    }

    override fun setupActionBar() {
        setSupportActionBar(binding.callLogAppBar.toolbar)
        super.setupActionBar()
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
//            it.title = getString(R.string.call_logs)
            it.title = "Call Logs"
        }
        binding.callLogAppBar.toolbar.setNavigationOnClickListener { finishAfterTransition() }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.btnCallLogChat) {
            startChatActivity(adapter.getItem(position))
        }
    }

    private fun startChatActivity(callLog: RtcCallLog) {
        val args = RtcArgs()
        args.doctorUuid = callLog.callerId
        args.patientId = callLog.roomId
        args.patientName = callLog.roomName
//        args.visitId = VisitDAO().getVisitIdByPatientId(args.patientId)
        args.nurseId = callLog.calleeId
        IDAChatActivity.startChatActivity(this, args)
    }
}