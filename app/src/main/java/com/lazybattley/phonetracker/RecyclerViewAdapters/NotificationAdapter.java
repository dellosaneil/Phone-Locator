package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lazybattley.phonetracker.HelperClasses.PendingRequestHelperClass;
import com.lazybattley.phonetracker.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<PendingRequestHelperClass> receivedRequests;
    private NotificationClick notificationClick;

    public NotificationAdapter(NotificationClick notificationClick) {
        this.notificationClick = notificationClick;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
        return new NotificationViewHolder(view, notificationClick);
    }


    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String user = receivedRequests.get(position).getEmail();
        long time = receivedRequests.get(position).getTime();
        String date = DateFormat.getDateInstance().format(time);
        String hour = timeFormat(time);
        holder.rowNotification_user.setText(user);
        holder.rowNotification_imageType.setImageResource(R.drawable.ic_invite);
        holder.rowNotification_timeSent.setText(context.getString(R.string.request_location_time_sent, date, hour));
    }


    private String timeFormat(long time) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone(String.valueOf(TimeZone.getDefault())));
        return formatter.format(new Date(time));
    }

    public void setRequests(List<PendingRequestHelperClass> receivedRequests){
        this.receivedRequests = receivedRequests;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (receivedRequests == null) {
            return 0;
        }
        return receivedRequests.size();
    }


    public static class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView rowNotification_user;
        private TextView rowNotification_timeSent;
        private ImageView rowNotification_imageType;
        private NotificationClick notificationClick;

        public NotificationViewHolder(@NonNull View itemView, NotificationClick notificationClick) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.notificationClick = notificationClick;
            rowNotification_user = itemView.findViewById(R.id.rowNotification_user);
            rowNotification_imageType = itemView.findViewById(R.id.rowNotification_imageType);
            rowNotification_timeSent = itemView.findViewById(R.id.rowNotification_timeSent);
        }

        @Override
        public void onClick(View view) {
            notificationClick.onClickNotification(getAdapterPosition());
        }
    }

    public interface NotificationClick{
        void onClickNotification(int position);


    }















}
