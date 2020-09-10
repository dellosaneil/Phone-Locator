package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationHelperClass;
import com.lazybattley.phonetracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RequestLocationAdapter extends RecyclerView.Adapter<RequestLocationAdapter.RequestLocationViewHolder> {
    private List<RequestLocationHelperClass> sentRequests;

    public RequestLocationAdapter(List<RequestLocationHelperClass> sentRequests) {
        this.sentRequests = sentRequests;
    }

    @NonNull
    @Override
    public RequestLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_request_location, parent, false);
        return new RequestLocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestLocationViewHolder holder, int position) {
        RequestLocationHelperClass temp = sentRequests.get(position);
        setStatus(temp.getStatus(), holder);
        setTime(temp.getTimeSent(), holder);
        holder.requestLocation_email.setText(temp.getEmail());


    }

    private void setStatus(String status, RequestLocationViewHolder holder) {
        switch (status) {
            case "Pending":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_pending_request);
                break;
            case "Accepted":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_accepted);
                break;
            case "Declined":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_denied);
                break;
        }
        holder.requestLocation_currentStatus.setText(status);
    }

    private void setTime(long time, RequestLocationViewHolder holder) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String dateString = formatter.format(new Date(time));
        holder.requestLocation_dateSent.setText("Time Sent: " + dateString);
    }


    @Override
    public int getItemCount() {
        if(sentRequests == null){
            return 0;
        }
        return sentRequests.size();
    }

    public static class RequestLocationViewHolder extends RecyclerView.ViewHolder {
        private ImageView requestLocation_imageStatus;
        private MaterialTextView requestLocation_email;
        private MaterialTextView requestLocation_dateSent;
        private MaterialTextView requestLocation_currentStatus;

        public RequestLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            requestLocation_imageStatus = itemView.findViewById(R.id.requestLocation_imageStatus);
            requestLocation_email = itemView.findViewById(R.id.requestLocation_username);
            requestLocation_dateSent = itemView.findViewById(R.id.requestLocation_dateSent);
            requestLocation_currentStatus = itemView.findViewById(R.id.requestLocation_currentStatus);
        }
    }
}
