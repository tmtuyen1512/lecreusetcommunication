/*
 * Copyright 2016. Alejandro Sánchez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.delfi.xmobile.app.lecreusetcommunication.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;

import java.util.List;

/**
 * Class Description.
 *
 * @author asanchezyu@gmail.com.
 * @version 1.0.
 * @since 15/6/16.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private List<FTPSetting.FilesTo> dataList;

    public MessageAdapter(List<FTPSetting.FilesTo> dataList) {
        this.dataList = dataList;
    }

    public List<FTPSetting.FilesTo> getDataList(){
        return this.dataList;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.item_message_listview, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        view.setLayoutParams(lp);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind( dataList.get( position ) );
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder{

        private final Context context;
        private TextView tvLabel, tvFileName, tvStatus;

        public ViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(final FTPSetting.FilesTo data){

            if(data.Label == null)
                tvLabel.setVisibility(View.GONE);
            else {
                tvLabel.setVisibility(View.VISIBLE);
                tvLabel.setText(data.Label);
            }
            tvFileName.setText(data.DisplayName);
            tvStatus.setText(data.message == null ? (data.isTransfer ? context.getResources().getString(R.string.success) : context.getResources().getString(R.string.not_success)) : data.message);
            if(data.isSameFile) {
                tvStatus.setText(R.string.skip_same_file);
            }
            if(!data.isTransfer) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.red));
            }
            else {
                tvStatus.setTextColor(context.getResources().getColor(R.color.green));
            }
        }
    }

}
