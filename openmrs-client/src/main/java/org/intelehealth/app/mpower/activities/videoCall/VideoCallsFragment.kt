package org.intelehealth.app.mpower.activities.videoCall


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.openmrs.android_sdk.library.OpenmrsAndroid
import com.openmrs.android_sdk.library.models.CallTokenModel
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Patient
import com.openmrs.android_sdk.library.models.Result
import com.openmrs.android_sdk.library.models.SearchRequest
import com.openmrs.android_sdk.utilities.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.activities.BaseFragment
import org.intelehealth.app.mpower.databinding.FragmentVideoCallListBinding
import org.intelehealth.app.mpower.utilities.makeGone
import org.intelehealth.app.mpower.utilities.makeInvisible
import org.intelehealth.app.mpower.utilities.makeVisible
import org.intelehealth.app.mpower.webrtc.activity.CallNotificationTestActivity
import org.intelehealth.fcm.utils.FcmTokenGenerator.getDeviceToken
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.UUID

@AndroidEntryPoint
class VideoCallsFragment : BaseFragment(),  VideoCallsRecyclerAdapter.OnCallButtonClickListener{
    private var _binding: FragmentVideoCallListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VideoCallsViewModel by viewModels()

    companion object {
        fun newInstance(): VideoCallsFragment {
            return VideoCallsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVideoCallListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(this.activity)
        with(binding) {
            videoCallListRecyclerView.setHasFixedSize(true)
            videoCallListRecyclerView.layoutManager = linearLayoutManager
            videoCallListRecyclerView.adapter =
                VideoCallsRecyclerAdapter(
                    this@VideoCallsFragment, ArrayList(), this@VideoCallsFragment,
                )

            setupObserver()
            fetchMembers()

            videoCallSwipeLayout.setOnRefreshListener {
                fetchMembersOnRefresh("")
                videoCallSwipeLayout.isRefreshing = false
            }
        }
    }

    private fun setupObserver() {
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Result.Loading -> showLoading()
                is Result.Success -> showMemberList(result.data)
                else -> showError()
            }
        })
    }


    fun fetchMembers() {
        viewModel.fetchMembers()
    }

    fun fetchMembersOnRefresh(query: String) {
        viewModel.fetchMembers(query)
    }

    fun fetchMembers(query: String) {
        viewModel.fetchMembers(query)
    }

    private fun showLoading() {
        with(binding) {
            callListProgressBar.makeInvisible()
            videoCallListRecyclerView.makeGone()
        }
    }

    private fun showMemberList(patients: List<Patient>) {
        with(binding) {
            callListProgressBar.makeGone()
            if (patients.isEmpty()) {
                videoCallListRecyclerView.makeGone()
                showEmptyListText()
            } else {
                (videoCallListRecyclerView.adapter as VideoCallsRecyclerAdapter).updateList(patients)
                videoCallListRecyclerView.makeVisible()
                hideEmptyListText()
            }
        }
    }

    private fun showError() {
        with(binding) {
            callListProgressBar.makeGone()
            videoCallListRecyclerView.makeGone()
        }
        showEmptyListText()
    }

    private fun showEmptyListText() {
        binding.emptyCallList.makeVisible()
        binding.emptyCallList.text = getString(R.string.search_member_no_results)
    }

    private fun hideEmptyListText() {
        binding.emptyCallList.makeGone()
    }

    override fun onCallButtonClick(patient: Patient) {
        patient.display?.let { ToastUtil.success(it) }
        getCallToken(patient)
    }



    private fun getCallToken(patient: Patient) {
        val tid = UUID.randomUUID().toString()
        val did = OpenmrsAndroid.getProviderId()
        val pid = if(patient.uuid != null) patient.uuid else ""
        viewModel.onGenerateToken(CallTokenModel(did, tid, pid!!))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

