package org.openmrs.mobile.activities.memberList

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.activity.viewModels
import com.openmrs.android_sdk.utilities.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.ACBaseActivity
import org.openmrs.mobile.databinding.ActivityMemberListBinding
import androidx.databinding.DataBindingUtil


@AndroidEntryPoint
class MemberListActivity : ACBaseActivity(), View.OnClickListener {
    private var query: String? = null
    private lateinit var mBinding: ActivityMemberListBinding
    private val mViewModel: MemberListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_member_list)
        mBinding.viewModel = mViewModel

        supportActionBar?.let {
            it.elevation = 0f
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.action_member_list)
        }
        // Create fragment
        var memberListFragment = supportFragmentManager.findFragmentById(R.id.membersContentFrame) as MemberListFragment?
        if (memberListFragment == null) {
            memberListFragment = MemberListFragment.newInstance()
        }
        if (!memberListFragment.isActive) {
            addFragmentToActivity(supportFragmentManager,
                memberListFragment, R.id.membersContentFrame)
        }

        observeData()
    }

    private fun observeData() {}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.actionAddPatients -> {

            }
            android.R.id.home -> onBackPressed()
            else -> {
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.find_locally_and_add_patients_menu, menu)


        val searchMenuItem = menu.findItem(R.id.actionSearchLocal)
        val searchView = menu.findItem(R.id.actionSearchLocal).actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                val memberListFragment = supportFragmentManager.findFragmentById(R.id.membersContentFrame) as MemberListFragment?
                if(NetworkUtils.isOnline()){
                    memberListFragment?.fetchMembersOnRefresh(query)
                } else {
                    memberListFragment?.fetchMembers(query)
                }
                return true
            }
        })
        return true
    }

    override fun onClick(v: View?) {
        val contentDashboard = mBinding.membersContentFrame
        when (v?.id) {
//            contentDashboard.fabAddMember.id -> {}
        }
    }
}