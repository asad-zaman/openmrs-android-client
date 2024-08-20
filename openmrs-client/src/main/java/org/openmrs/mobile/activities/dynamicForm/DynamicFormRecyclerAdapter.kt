package org.openmrs.mobile.activities.dynamicForm

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.marginBottom
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.openmrs.android_sdk.library.models.Encounter
import com.openmrs.android_sdk.library.models.Observation
import com.openmrs.android_sdk.utilities.ApplicationConstants
import org.openmrs.mobile.R
import org.openmrs.mobile.listeners.ItemClickListener
import org.openmrs.mobile.utilities.ViewUtils

class DynamicFormRecyclerAdapter(private val observationList: List<Observation>) : RecyclerView.Adapter<ObservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dynamic_textview, parent, false)
        return ObservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        val mObservation = observationList[position]
        holder.bind(mObservation)
    }

    override fun getItemCount(): Int {
        return observationList.size
    }
}

class ObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvObserver: TextView = itemView.findViewById(R.id.dynamicTV)

    fun bind(observation: Observation) {
        tvObserver.text = observation.display
    }
}