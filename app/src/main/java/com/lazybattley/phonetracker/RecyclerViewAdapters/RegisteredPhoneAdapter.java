package com.lazybattley.phonetracker.RecyclerViewAdapters;

import android.content.Context;
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
import com.lazybattley.phonetracker.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisteredPhoneAdapter extends RecyclerView.Adapter<RegisteredPhoneAdapter.RegisteredPhoneViewHolder> {

    private Context context;
    private List<String> model;
    private List<LatLng> coordinates;
    private List<Integer> batteryLevel;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;


    public RegisteredPhoneAdapter(Context context, List<String> model, List<LatLng> coordinates, List<Integer> batteryLevel, RecyclerView recyclerView, ProgressBar progressBar) {
        this.context = context;
        this.model = model;
        this.coordinates = coordinates;
        this.batteryLevel = batteryLevel;
        this.recyclerView = recyclerView;
        this.progressBar = progressBar;
    }

    @NonNull
    @Override
    public RegisteredPhoneAdapter.RegisteredPhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_registered_phones, parent, false);
        return new RegisteredPhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredPhoneAdapter.RegisteredPhoneViewHolder holder, int position) {
        String phoneModel = model.get(position);
        LatLng location = coordinates.get(position);
        final String[] address = {null};
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        if (position == model.size() - 1) {
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }

        final LatLng finalLocation = location;
        final String finalPhoneModel = phoneModel;

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
                    holder.registeredPhoneAdapter_phoneModel.setText(finalPhoneModel);
                    holder.registeredPhoneAdapter_phoneLocation.setText(address[0]);
                    holder.registeredPhoneAdapter_batteryLevel.setText(context.getText(R.string.registered_phone_battery_level) + " " + batteryLevel.get(position) + "%");
                }
            });
        }).start();
    }


    @Override
    public int getItemCount() {
        if (model == null) {
            return 0;
        }
        return model.size();
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
