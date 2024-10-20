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

package org.intelehealth.app.mpower.activities.videoCall;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.openmrs.android_sdk.library.models.Patient;
import com.openmrs.android_sdk.utilities.ApplicationConstants;
import com.openmrs.android_sdk.utilities.DateUtils;

import org.intelehealth.app.mpower.R;

import java.util.List;

public class VideoCallsRecyclerAdapter extends RecyclerView.Adapter<VideoCallsRecyclerAdapter.VideoCallListViewHolder> {
    private VideoCallsFragment mContext;
    private List<Patient> mItems;
    private OnCallButtonClickListener callButtonClickListener;

    public VideoCallsRecyclerAdapter(VideoCallsFragment context, List<Patient> items, OnCallButtonClickListener listener) {
        this.mContext = context;
        this.mItems = items;
        this.callButtonClickListener = listener;
    }

    public interface OnCallButtonClickListener {
        void onCallButtonClick(Patient patient);
    }

    public void updateList(List<Patient> patientList) {
        this.mItems = patientList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoCallsRecyclerAdapter.VideoCallListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_video_call, parent, false);
        return new VideoCallListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCallsRecyclerAdapter.VideoCallListViewHolder holder, final int position) {
        holder.update(mItems.get(position));

        final Patient patient = mItems.get(position);

        if (null != patient.getUuid()) {
            holder.callerUUId.setText(patient.getUuid());
        }
        if (null != patient.getDisplay()) {
            holder.callerName.setText(patient.getDisplay());
        }

        holder.callBtn.setOnClickListener(v -> {
            if (callButtonClickListener != null) {
                callButtonClickListener.onCallButtonClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class VideoCallListViewHolder extends RecyclerView.ViewHolder {
        private TextView callerName;
        private TextView callerUUId;
        private ImageView callBtn;

        public VideoCallListViewHolder(View itemView) {
            super(itemView);
            callerName = itemView.findViewById(R.id.tv_caller_name);
            callerUUId = itemView.findViewById(R.id.tv_caller_uuid);
            callBtn = itemView.findViewById(R.id.iv_call_btn);
        }

        void update(final Patient value) {
            itemView.setOnClickListener(view -> {
            });
        }
    }
}
