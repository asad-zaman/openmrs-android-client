package org.intelehealth.app.mpower.activities.providerdashboard.providerrelationship

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.mpower.R
import org.intelehealth.app.mpower.activities.BaseFragment

@AndroidEntryPoint
class ProviderRelationshipFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_provider_relationship, null)
    }
}
