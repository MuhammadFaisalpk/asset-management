package com.app.assetmaintenance.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.assetmaintenance.admin.AdminComplainDetail;
import com.app.assetmaintenance.R;
import com.app.assetmaintenance.user.UserComplainDetail;
import com.app.assetmaintenance.models.ComplainsModel;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ComplainsAdapter extends RecyclerView.Adapter<ComplainsAdapter.MyViewHolder> {

    private final ArrayList<ComplainsModel> complains_array;
    Activity mContext;
    boolean isAdmin;

    public ComplainsAdapter(Activity Activity, ArrayList<ComplainsModel> newList, boolean admin) {
        this.mContext = Activity;
        this.complains_array = newList;
        this.isAdmin = admin;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_complains_item,
                parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        ComplainsModel complainsModel = complains_array.get(listPosition);

        int Id = complainsModel.getId();
        holder.date.setText(complainsModel.getCreated_at());
        holder.desc.setText(complainsModel.getDescription());
        holder.status.setText(complainsModel.getStatus());

        String image = complainsModel.getImage();
        Glide.with(mContext).load(image).fitCenter().into(holder.image);

        holder.details.setOnClickListener(view -> {
            viewDetails(Id);
        });
        holder.itemView.setOnClickListener(view -> {
            viewDetails(Id);
        });
    }

    private void viewDetails(int id) {
        Intent intent;
        if (isAdmin) {
            intent = new Intent(mContext, AdminComplainDetail.class);
        } else {
            intent = new Intent(mContext, UserComplainDetail.class);
        }
        intent.putExtra("complain_id", id);
        mContext.startActivity(intent);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView date, desc, details, status;

        public MyViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            date = (TextView) view.findViewById(R.id.date);
            desc = (TextView) view.findViewById(R.id.desc);
            status = (TextView) view.findViewById(R.id.status);
            details = (TextView) view.findViewById(R.id.details);
        }
    }

    @Override
    public int getItemCount() {
        return complains_array.size();
    }
}

