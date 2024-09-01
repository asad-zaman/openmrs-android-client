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

package org.openmrs.mobile.activities.referedMemberList;

import static org.openmrs.mobile.utilities.ViewUtils.adjustOpacity;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.openmrs.android_sdk.library.models.Patient;
import com.openmrs.android_sdk.utilities.ApplicationConstants;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.memberProfile.MemberProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class ReferedMembersRecyclerViewAdapter extends RecyclerView.Adapter<ReferedMembersRecyclerViewAdapter.PatientViewHolder> {
    private ReferedMembersFragment mContext;
    private List<Patient> mItems;
    private boolean multiSelect = false;
    private ArrayList<Patient> selectedItems = new ArrayList<>();

    public ReferedMembersRecyclerViewAdapter(ReferedMembersFragment context, List<Patient> items) {
        this.mContext = context;
        this.mItems = items;
    }

    public void updateList(List<Patient> patientList) {
        this.mItems = patientList;
        this.selectedItems = new ArrayList();
        this.multiSelect = false;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReferedMembersRecyclerViewAdapter.PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_refered_patient, parent, false);
        return new PatientViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReferedMembersRecyclerViewAdapter.PatientViewHolder holder, final int position) {
        holder.update(mItems.get(position));

        final Patient patient = mItems.get(position);

        if (null != patient.getIdentifier()) {
            String patientIdentifier = String.format(mContext.getResources().getString(R.string.patient_identifier),
                    patient.getIdentifier().getIdentifier());
            holder.mIdentifier.setText(patientIdentifier);
        }
        if (null != patient.getName()) {
            holder.mDisplayName.setText(patient.getPerson().getDisplay());
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
            holder.mRowLayout.setCardBackgroundColor(mContext.getResources().getColor(R.color.deceased_green));
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class PatientViewHolder extends RecyclerView.ViewHolder {
        private CardView mRowLayout;
        private TextView mIdentifier;
        private TextView mDisplayName;
        private ImageView mGender;

        public PatientViewHolder(View itemView) {
            super(itemView);
            mRowLayout = (CardView) itemView;
            mRowLayout.setBackgroundColor(adjustOpacity(Color.GREEN, 0.05f));
            mIdentifier = itemView.findViewById(R.id.referedPatientIdentifier);
            mDisplayName = itemView.findViewById(R.id.referedPatientName);
            mGender = itemView.findViewById(R.id.referedPatientGender);
        }

        void selectItem(Patient item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    mRowLayout.setBackgroundColor(adjustOpacity(Color.GREEN, 0.05f));
                } else {
                    selectedItems.add(item);
                    mRowLayout.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected_card));
                }
            }
        }

        void update(final Patient value) {
            if (selectedItems.contains(value)) {
                mRowLayout.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected_card));
            } else {
                mRowLayout.setBackgroundColor(adjustOpacity(Color.GREEN, 0.05f));
            }
            itemView.setOnClickListener(view -> {
                if (!multiSelect) {
                    Intent intent = new Intent(mContext.getActivity(), MemberProfileActivity.class);
                    try{
                        String personObj = new Gson().toJson(value);
                        intent.putExtra(ApplicationConstants.BundleKeys.PATIENT_ENTITY, personObj);
                    } catch (Exception e) {
                        Log.d("", e.toString());
                    }
                    mContext.startActivity(intent);
                } else {
                    selectItem(value);
                }
            });
        }
    }
}
