package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lazybattley.phonetracker.R;

import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder> {

    private Context context;
    private List<String> permittedPeople;


    @NonNull
    @Override
    public UserProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view  = LayoutInflater.from(context).inflate(R.layout.row_user_profile, parent, false);
        return new UserProfileViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull UserProfileViewHolder holder, int position) {
        holder.userProfile_adapterName.setText(permittedPeople.get(position));
    }

    @Override
    public int getItemCount() {
        if(permittedPeople == null){
            return 0;
        }
        return this.permittedPeople.size();
    }

    public void setPermittedPeople(List<String> permittedPeople){
        this.permittedPeople = permittedPeople;
        notifyDataSetChanged();
    }

    public static class UserProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView userProfile_adapterName;


        public UserProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfile_adapterName = itemView.findViewById(R.id.userProfile_adapterName);


        }
    }
}
