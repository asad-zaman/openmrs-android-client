/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.activities.memberList;


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.openmrs.android_sdk.library.models.Patient;
import com.openmrs.android_sdk.utilities.ApplicationConstants;
import com.openmrs.android_sdk.utilities.DateUtils;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MemberListRecyclerViewAdapter extends RecyclerView.Adapter<MemberListRecyclerViewAdapter.MemberListViewHolder> {
    private MemberListFragment mContext;
    private List<Patient> mItems;

    public MemberListRecyclerViewAdapter(MemberListFragment context, List<Patient> items) {
        this.mContext = context;
        this.mItems = items;
    }

    public void updateList(List<Patient> patientList) {
        this.mItems = patientList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberListRecyclerViewAdapter.MemberListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_find_synced_patients, parent, false);
        return new MemberListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberListRecyclerViewAdapter.MemberListViewHolder holder, final int position) {
        holder.update(mItems.get(position));

        final Patient patient = mItems.get(position);

        if (null != patient.getIdentifier()) {
            String patientIdentifier = String.format(mContext.getResources().getString(R.string.patient_identifier),
                    patient.getIdentifier().getIdentifier());
            holder.mIdentifier.setText(patientIdentifier);
        }
        if (null != patient.getName()) {
            holder.mDisplayName.setText(patient.getName().getNameString());
        }
        if (null != patient.getGender()) {
            if (patient.getPhoto() != null) {
                holder.mGender.setImageBitmap(patient.getPhoto());
            } else {
                if (patient.getGender().equals(ApplicationConstants.MALE)) {
                    holder.mGender.setImageResource(R.drawable.patient_male);
                } else {
                    holder.mGender.setImageResource(R.drawable.patient_female);
                }
            }
        } else {
            holder.mGender.setImageResource(R.drawable.patient_male);
        }
        if (patient.isDeceased() != null && patient.isDeceased()) {
            holder.mRowLayout.setCardBackgroundColor(mContext.getResources().getColor(R.color.deceased_red));
        }
        try {
            holder.mBirthDate.setText(DateUtils.convertTime(DateUtils.convertTime(patient.getBirthdate())));
        } catch (Exception e) {
            holder.mBirthDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class MemberListViewHolder extends RecyclerView.ViewHolder {
        private CardView mRowLayout;
        private TextView mIdentifier;
        private TextView mDisplayName;
        private ImageView mGender;
        private TextView mBirthDate;

        public MemberListViewHolder(View itemView) {
            super(itemView);
            mRowLayout = (CardView) itemView;
            mIdentifier = itemView.findViewById(R.id.syncedPatientIdentifier);
            mDisplayName = itemView.findViewById(R.id.syncedPatientDisplayName);
            mGender = itemView.findViewById(R.id.syncedPatientGender);
            mBirthDate = itemView.findViewById(R.id.syncedPatientBirthDate);
        }

        void update(final Patient value) {
            itemView.setOnClickListener(view -> {
            });
        }
    }
}
