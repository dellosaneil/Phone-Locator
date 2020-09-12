package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lazybattley.phonetracker.HelperClasses.RequestLocationFriendHelperClass;
import com.lazybattley.phonetracker.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<RequestLocationFriendHelperClass> receivedRequests;

    public NotificationAdapter(Context context, List<RequestLocationFriendHelperClass> receivedRequests) {
        this.context = context;
        this.receivedRequests = receivedRequests;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
        return new NotificationViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String user = receivedRequests.get(position).getEmail();
        long time = receivedRequests.get(position).getTimeSent();

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


    @Override
    public int getItemCount() {
        if (receivedRequests.size() == 0) {
            return 0;
        }
        return receivedRequests.size();
    }


    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView rowNotification_user;
        private TextView rowNotification_timeSent;
        private ImageView rowNotification_imageType;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            rowNotification_user = itemView.findViewById(R.id.rowNotification_user);
            rowNotification_imageType = itemView.findViewById(R.id.rowNotification_imageType);
            rowNotification_timeSent = itemView.findViewById(R.id.rowNotification_timeSent);
        }


    }
}
