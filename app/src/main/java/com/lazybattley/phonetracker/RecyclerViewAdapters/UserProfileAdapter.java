package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lazybattley.phonetracker.R;

import java.util.List;

public class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.UserProfileViewHolder> {

    private List<String> permittedPeople;
    private OnPersonClicked onPersonClicked;

    public UserProfileAdapter(OnPersonClicked onPersonClicked) {
        this.onPersonClicked = onPersonClicked;
    }

    @NonNull
    @Override
    public UserProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_profile, parent, false);
        return new UserProfileViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull UserProfileViewHolder holder, int position) {
        holder.userProfile_fullName.setText(permittedPeople.get(position));
        holder.userProfile_imageLogo.setImageResource(R.drawable.ic_delete);
        holder.userProfile_imageLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPersonClicked.removeFromList(position);
            }
        });

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
        private TextView userProfile_fullName;
        private ImageButton userProfile_imageLogo;

        public UserProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfile_fullName = itemView.findViewById(R.id.userProfile_fullName);
            userProfile_imageLogo = itemView.findViewById(R.id.userProfile_imageLogo);
        }
    }
    public interface OnPersonClicked{
        void removeFromList(int position);
    }
}
