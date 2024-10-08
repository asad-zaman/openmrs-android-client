package org.intelehealth.app.mpower.activities.memberList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.openmrs.android_sdk.library.models.Patient
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.mpower.activities.BaseFragment
import org.intelehealth.app.mpower.databinding.FragmentMemberListBinding
import com.openmrs.android_sdk.library.models.Result
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.utilities.makeGone
import org.intelehealth.app.mpower.utilities.makeInvisible
import org.intelehealth.app.mpower.utilities.makeVisible

@AndroidEntryPoint
class MemberListFragment : BaseFragment() {
    private var _binding: FragmentMemberListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemberListViewModel by viewModels()

    companion object {
        fun newInstance(): MemberListFragment {
            return MemberListFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMemberListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val linearLayoutManager = LinearLayoutManager(this.activity)
        with(binding) {
            memberListRecyclerView.setHasFixedSize(true)
            memberListRecyclerView.layoutManager = linearLayoutManager
            memberListRecyclerView.adapter =
                MemberListRecyclerViewAdapter(
                    this@MemberListFragment,
                    ArrayList()
                )

            setupObserver()
            fetchMembers()

            memberSwipeLayout.setOnRefreshListener {
                fetchMembersOnRefresh("")
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

    fun fetchMembersOnRefresh(query: String) {
        viewModel.fetchMembers(query)
    }

    fun fetchMembers(query: String) {
        viewModel.fetchMembers(query)
    }

    private fun showLoading() {
        with(binding) {
            memberListProgressBar.makeInvisible()
            memberListRecyclerView.makeGone()
        }
    }

    private fun showMemberList(patients: List<Patient>) {
        with(binding) {
            memberListProgressBar.makeGone()
            if (patients.isEmpty()) {
                memberListRecyclerView.makeGone()
                showEmptyListText()
            } else {
                (memberListRecyclerView.adapter as MemberListRecyclerViewAdapter).updateList(patients)
                memberListRecyclerView.makeVisible()
                hideEmptyListText()
            }
        }
    }

    private fun showError() {
        with(binding) {
            memberListProgressBar.makeGone()
            memberListRecyclerView.makeGone()
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