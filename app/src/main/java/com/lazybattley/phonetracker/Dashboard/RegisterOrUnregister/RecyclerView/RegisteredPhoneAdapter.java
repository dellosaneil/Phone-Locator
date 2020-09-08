package com.lazybattley.phonetracker.Dashboard.RegisterOrUnregister.RecyclerView;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.lazybattley.phonetracker.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisteredPhoneAdapter extends RecyclerView.Adapter<RegisteredPhoneAdapter.RegisteredPhoneViewHolder> {

    private Context context;
    private List<Map<String, LatLng>> phoneDetails;
    private List<Integer> batteryLevel;


    public RegisteredPhoneAdapter(Context context, List<Map<String, LatLng>> phoneDetails, List<Integer> batteryLevel) {
        this.context = context;
        this.phoneDetails = phoneDetails;
        this.batteryLevel = batteryLevel;
    }

    @NonNull
    @Override
    public RegisteredPhoneAdapter.RegisteredPhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.registered_phones_row_layout, parent,false);
        return new RegisteredPhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredPhoneAdapter.RegisteredPhoneViewHolder holder, int position) {
        String phoneModel = null;
        LatLng location = null;
        final String[] address = {null};
        for(String model : phoneDetails.get(position).keySet()){
            phoneModel = model;
            location = phoneDetails.get(position).get(phoneModel);
        }
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        final LatLng finalLocation = location;
        final String finalPhoneModel = phoneModel;

        new Thread(() -> {
            try {
                List<Address> list = geocoder.getFromLocation(finalLocation.latitude, finalLocation.longitude, 1);
                if (!list.isEmpty()) {
                    address[0] = list.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                address[0] = R.string.error_geolocation + "";
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    holder.registeredPhoneAdapter_phoneModel.setText(finalPhoneModel);
                    holder.registeredPhoneAdapter_phoneLocation.setText(address[0]);
                    holder.registeredPhoneAdapter_batteryLevel.setText(context.getText(R.string.registered_phone_battery_level)+" "+batteryLevel.get(position)+"%");
                }
            });
        }).start();
    }

    @Override
    public int getItemCount() {
        if(phoneDetails == null){
            return 0;
        }
        return phoneDetails.size();
    }


    public static class RegisteredPhoneViewHolder extends RecyclerView.ViewHolder {
        private TextView registeredPhoneAdapter_phoneModel;
        private TextView registeredPhoneAdapter_phoneLocation;
        private TextView registeredPhoneAdapter_batteryLevel;

        public RegisteredPhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            registeredPhoneAdapter_phoneModel = itemView.findViewById(R.id.registeredPhoneAdapter_phoneModel);
            registeredPhoneAdapter_phoneLocation = itemView.findViewById(R.id.registeredPhoneAdapter_phoneLocation);
            registeredPhoneAdapter_batteryLevel = itemView.findViewById(R.id.registeredPhoneAdapter_batteryLevel);
        }
    }
}
