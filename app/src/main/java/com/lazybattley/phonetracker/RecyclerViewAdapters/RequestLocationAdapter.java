package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.lazybattley.phonetracker.HelperClasses.SentRequestHelperClass;
import com.lazybattley.phonetracker.R;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RequestLocationAdapter extends RecyclerView.Adapter<RequestLocationAdapter.RequestLocationViewHolder> {
    private Context context;
    private List<SentRequestHelperClass> sentRequests;
    private RequestLocationInterface requestLocationInterface;

    public RequestLocationAdapter(RequestLocationInterface requestLocationInterface) {
        this.sentRequests = new ArrayList<>();
        this.requestLocationInterface = requestLocationInterface;
    }

    @NonNull
    @Override
    public RequestLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_request_location, parent, false);
        return new RequestLocationViewHolder(view, requestLocationInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestLocationViewHolder holder, int position) {
        SentRequestHelperClass temp = sentRequests.get(position);
        setStatus(temp.getStatus(), holder);
        setTime(temp.getTimeSent(), holder);
        holder.requestLocation_email.setText(temp.getEmail());
        if (position == sentRequests.size() - 1) {
            requestLocationInterface.finishedLoading();
        }
    }

    public void setRequests(List<SentRequestHelperClass> requests) {
        this.sentRequests = requests;
        notifyDataSetChanged();
    }


    private void setStatus(String status, RequestLocationViewHolder holder) {
        switch (status) {
            case "Pending":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_pending_request);
                holder.requestLocation_currentStatus.setTextColor(Color.BLACK);
                break;
            case "Accepted":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_accepted);
                holder.requestLocation_currentStatus.setTextColor(Color.GREEN);
                break;
            case "Declined":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_denied);
                holder.requestLocation_currentStatus.setTextColor(Color.RED);
                break;
            case "Removed":
                holder.requestLocation_imageStatus.setImageResource(R.drawable.ic_remove);
                holder.requestLocation_currentStatus.setTextColor(Color.RED);
        }
        holder.requestLocation_currentStatus.setText(status);

    }

    private void setTime(long time, RequestLocationViewHolder holder) {
        String date = DateFormat.getDateInstance().format(time);
        String hour = timeFormat(time);
        holder.requestLocation_dateSent.setText(context.getString(R.string.request_location_time_sent, date, hour));
    }

    private String timeFormat(long time) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
        return formatter.format(new Date(time));
    }


    @Override
    public int getItemCount() {
        if (sentRequests.size() == 0) {
            requestLocationInterface.finishedLoading();
            return 0;
        }
        return sentRequests.size();
    }


    public static class RequestLocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RequestLocationInterface requestLocationInterface;
        private ImageView requestLocation_imageStatus;
        private MaterialTextView requestLocation_email;
        private MaterialTextView requestLocation_dateSent;
        private MaterialTextView requestLocation_currentStatus;

        public RequestLocationViewHolder(@NonNull View itemView, RequestLocationInterface requestLocationInterface) {
            super(itemView);
            this.requestLocationInterface = requestLocationInterface;
            requestLocation_imageStatus = itemView.findViewById(R.id.requestLocation_imageStatus);
            requestLocation_email = itemView.findViewById(R.id.requestLocation_username);
            requestLocation_dateSent = itemView.findViewById(R.id.requestLocation_dateSent);
            requestLocation_currentStatus = itemView.findViewById(R.id.requestLocation_currentStatus);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            requestLocationInterface.requestClicked(getAdapterPosition());
        }
    }

    public interface RequestLocationInterface {
        void requestClicked(int position);

        void finishedLoading();
    }

}
