package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.lazybattley.phonetracker.HelperClasses.CurrentLocationHelperClass;
import com.lazybattley.phonetracker.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CurrentLocationAdapter extends RecyclerView.Adapter<CurrentLocationAdapter.CurrentLocationViewHolder> {

    private Context context;
    private List<CurrentLocationHelperClass> details;


    @NonNull
    @Override
    public CurrentLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_current_location_summary, parent, false);
        context = parent.getContext();
        return new CurrentLocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentLocationViewHolder holder, int position) {
        CurrentLocationHelperClass detail = details.get(position);
        String[] currentTime = milliSecondsToDate(detail.getLastUpdated());
        String address = getGeoCode(detail.getCoordinates());
        String fullName = detail.getFullName();


       holder.currentLocation_exactTime.setText(context.getString(R.string.current_location_summary_time, currentTime[0], currentTime[1]));
       holder.currentLocation_location.setText(address);
       holder.currentLocation_fullName.setText(fullName);
       holder.currentLocation_image.setImageResource(R.drawable.ic_location);
    }

    private String[] milliSecondsToDate(long time){
        String[] currentTime = new String[2];
        Date currentDate = new Date(time);
        DateFormat df = new SimpleDateFormat("dd/MM/yy");
        currentTime[0] = df.format(currentDate);
        df = new SimpleDateFormat("HH:mm:ss");
        currentTime[1] = df.format(currentDate);
        return currentTime;
    }

    private String getGeoCode(LatLng coordinates){
        String address = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try{
            List<Address> list = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
            if(!list.isEmpty()){
                address = list.get(0).getAddressLine(0);
            }
        }catch(IOException e){
            e.getMessage();
        }
        return address;
    }


    public void setData(List<CurrentLocationHelperClass> details){
        this.details = details;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        if(details == null){
            return 0;
        }
        return details.size();
    }

    public static class CurrentLocationViewHolder extends RecyclerView.ViewHolder {

        private TextView currentLocation_exactTime;
        private TextView currentLocation_location;
        private TextView currentLocation_fullName;
        private ImageView currentLocation_image;

        public CurrentLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            currentLocation_exactTime = itemView.findViewById(R.id.currentLocation_exactTime);
            currentLocation_location = itemView.findViewById(R.id.currentLocation_location);
            currentLocation_fullName = itemView.findViewById(R.id.currentLocation_fullName);
            currentLocation_image = itemView.findViewById(R.id.currentLocation_image);
        }
    }
}
