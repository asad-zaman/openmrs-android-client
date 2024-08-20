package org.openmrs.mobile.activities.memberProfile

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.openmrs.android_sdk.library.models.Encounter
import com.openmrs.android_sdk.utilities.ApplicationConstants
import com.openmrs.android_sdk.utilities.DateUtils.convertTime
import com.openmrs.android_sdk.utilities.ToastUtil
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.dynamicForm.DynamicFormActivity
import org.openmrs.mobile.utilities.ViewUtils.adjustOpacity

class ServiceHistoryRecyclerAdapter(private val mContext: ServiceHistoryFragment, private var mItems: List<Encounter>) : RecyclerView.Adapter<ServiceHistoryRecyclerAdapter.ServiceHistoryViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceHistoryViewHolder {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.row_service_history, parent, false)
        return ServiceHistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceHistoryViewHolder, position: Int) {
        val encounter = mItems[position]
        if (null != encounter.display) {
            holder.tvEncounterDisplay.text = encounter.form?.display
        }
        if (null != encounter.encounterDatetime) {
            try {
                holder.tvEncounterDatetime.text = convertTime(encounter.encounterDatetime!!)
            } catch (e: Exception) {
                holder.tvEncounterDatetime.text = ""
            }
        }
        holder.update(mItems[position])
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ServiceHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mRowLayout: CardView
        val tvEncounterDisplay: TextView
        val tvEncounterDatetime: TextView

        fun update(value: Encounter?) {

            itemView.setOnClickListener {
                val intent = Intent(mContext.activity, DynamicFormActivity::class.java)
                try {
                    val encounterObj = Gson().toJson(value)
                    intent.putExtra(ApplicationConstants.BundleKeys.ENCOUNTER_ENTITY, encounterObj)
                } catch (e: java.lang.Exception) {
                    Log.d("", e.toString())
                }
                mContext.startActivity(intent)
            }
        }

        init {
            mRowLayout = itemView as CardView
            mRowLayout.setBackgroundColor(adjustOpacity(Color.GREEN, 0.05f))
            tvEncounterDisplay = itemView.findViewById(R.id.encounterDisplayName)
            tvEncounterDatetime = itemView.findViewById(R.id.encounterDateTime)
        }
    }

    fun updateList(mList: List<Encounter>) {
        mItems = mList
        notifyDataSetChanged()
    }
}