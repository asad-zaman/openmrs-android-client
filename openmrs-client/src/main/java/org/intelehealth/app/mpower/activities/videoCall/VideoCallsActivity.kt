package org.intelehealth.app.mpower.activities.videoCall

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.activity.viewModels
import com.openmrs.android_sdk.utilities.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.activities.ACBaseActivity
import androidx.databinding.DataBindingUtil
import org.intelehealth.app.mpower.databinding.ActivityVideoCallListBinding


@AndroidEntryPoint
class VideoCallsActivity : ACBaseActivity(), View.OnClickListener {
    private var query: String? = null
    private lateinit var mBinding: ActivityVideoCallListBinding
    private val mViewModel: VideoCallsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_call_list)
        mBinding.viewModel = mViewModel

        supportActionBar?.let {
            it.elevation = 0f
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle("Call")
        }
        // Create fragment
        var callListFragment = supportFragmentManager.findFragmentById(R.id.callsContentFrame) as VideoCallsFragment?
        if (callListFragment == null) {
            callListFragment = VideoCallsFragment.newInstance()
        }
        if (!callListFragment.isActive) {
            addFragmentToActivity(supportFragmentManager, callListFragment, R.id.callsContentFrame)
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
                val memberListFragment = supportFragmentManager.findFragmentById(R.id.membersContentFrame) as VideoCallsFragment?
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
        val contentDashboard = mBinding.callsContentFrame
        when (v?.id) {
//            contentDashboard.fabAddMember.id -> {}
        }
    }
}