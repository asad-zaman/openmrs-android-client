package org.openmrs.mobile.activities.referedMemberList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.openmrs.android_sdk.library.models.Patient
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.activities.BaseFragment
import org.openmrs.mobile.databinding.FragmentMemberListBinding
import com.openmrs.android_sdk.library.models.Result
import kotlinx.android.synthetic.main.fragment_refered_members.*
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.syncedpatients.SyncedPatientsRecyclerViewAdapter
import org.openmrs.mobile.base.BaseRecyclerViewAdapter
import org.openmrs.mobile.databinding.FragmentReferedMembersBinding
import org.openmrs.mobile.utilities.makeGone
import org.openmrs.mobile.utilities.makeInvisible
import org.openmrs.mobile.utilities.makeVisible
import java.lang.reflect.Member

@AndroidEntryPoint
class ReferedMembersFragment : BaseFragment() {
    private var _binding: FragmentReferedMembersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReferedMembersViewModel by viewModels()

    companion object {
        fun newInstance(): ReferedMembersFragment {
            return ReferedMembersFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReferedMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(this.activity)
        with(binding) {
            referedMembersRecyclerView.setHasFixedSize(true)
            referedMembersRecyclerView.layoutManager = linearLayoutManager

//            referedMembersRecyclerView.adapter = BaseRecyclerViewAdapter<Patient>(
//                layoutId = R.layout.list_visit_item,
//                bind = { view, patient, position ->
////                     view.findViewById<TextView>(R.id.memberName).text = patient.name
//                }
//            )

            referedMembersRecyclerView.adapter = ReferedMembersRecyclerViewAdapter(this@ReferedMembersFragment, ArrayList())

            setupObserver()
            fetchMembers()

            memberSwipeLayout.setOnRefreshListener {
                fetchReferredMembersOnRefresh("")
                memberSwipeLayout.isRefreshing = false
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

    fun fetchReferredMembersOnRefresh(query: String) {
        viewModel.fetchReferredMembersOnRefresh(query)
    }

    fun fetchMembers(query: String) {
        viewModel.fetchMembers(query)
    }

    private fun showLoading() {
        with(binding) {
            memberListProgressBar.makeInvisible()
            referedMembersRecyclerView.makeGone()
        }
    }

    private fun showMemberList(patients: List<Patient>) {
        with(binding) {
            memberListProgressBar.makeGone()
            if (patients.isEmpty()) {
                referedMembersRecyclerView.makeGone()
                showEmptyListText()
            } else {
//                (referedMembersRecyclerView.adapter as BaseRecyclerViewAdapter<Patient>).updateItems(patients)
                (referedMembersRecyclerView.adapter as ReferedMembersRecyclerViewAdapter).updateList(patients)
                referedMembersRecyclerView.makeVisible()
                hideEmptyListText()
            }
        }
    }

    private fun showError() {
        with(binding) {
            memberListProgressBar.makeGone()
            referedMembersRecyclerView.makeGone()
        }
        showEmptyListText()
    }

    private fun showEmptyListText() {
        binding.emptyMemberList.makeVisible()
        binding.emptyMemberList.text = getString(R.string.search_member_no_results)
    }

    private fun hideEmptyListText() {
        binding.emptyMemberList.makeGone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}