package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.lazybattley.phonetracker.HelperClasses.OwnPhoneDetailsHelperClass;
import com.lazybattley.phonetracker.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisteredPhoneAdapter extends RecyclerView.Adapter<RegisteredPhoneAdapter.RegisteredPhoneViewHolder> {

    private Context context;
    private List<OwnPhoneDetailsHelperClass> ownPhone;
    private OnFinishedLoading onFinishedLoading;


    public RegisteredPhoneAdapter(List<OwnPhoneDetailsHelperClass> ownPhone, OnFinishedLoading onFinishedLoading) {
        this.ownPhone = ownPhone;
        this.onFinishedLoading = onFinishedLoading;
    }

    @NonNull
    @Override
    public RegisteredPhoneAdapter.RegisteredPhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.row_registered_phones, parent, false);

        return new RegisteredPhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredPhoneAdapter.RegisteredPhoneViewHolder holder, int position) {
        OwnPhoneDetailsHelperClass details = ownPhone.get(position);
        LatLng location = details.getCoordinates();
        final String[] address = {null};
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        if (position == ownPhone.size() - 1) {
            onFinishedLoading.dataFinishedLoading();
        }

        final LatLng finalLocation = location;

        new Thread(() -> {
            try {
                List<Address> list = geocoder.getFromLocation(finalLocation.latitude, finalLocation.longitude, 1);
                if (!list.isEmpty()) {
                    address[0] = list.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                address[0] = "Error: Reload Page";
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    holder.registeredPhoneAdapter_deviceName.setText(details.getDeviceName());
                    holder.registeredPhoneAdapter_phoneLocation.setText(address[0]);
                    holder.registeredPhoneAdapter_batteryLevel.setText(context.getString(R.string.registered_phone_battery_level, ownPhone.get(position).getBatteryLevel()));
                }
            });
        }).start();
    }


    @Override
    public int getItemCount() {
        if (ownPhone == null) {
            onFinishedLoading.dataFinishedLoading();
            return 0;
        }
        onFinishedLoading.dataFinishedLoading();
        return ownPhone.size();
    }

    public static class RegisteredPhoneViewHolder extends RecyclerView.ViewHolder {
        private TextView registeredPhoneAdapter_phoneLocation;
        private TextView registeredPhoneAdapter_batteryLevel;
        private TextView registeredPhoneAdapter_deviceName;

        public RegisteredPhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            registeredPhoneAdapter_phoneLocation = itemView.findViewById(R.id.registeredPhoneAdapter_phoneLocation);
            registeredPhoneAdapter_batteryLevel = itemView.findViewById(R.id.registeredPhoneAdapter_batteryLevel);
            registeredPhoneAdapter_deviceName = itemView.findViewById(R.id.registeredPhoneAdapter_deviceName);
        }
    }
    public interface OnFinishedLoading{
        void dataFinishedLoading();
    }

}
