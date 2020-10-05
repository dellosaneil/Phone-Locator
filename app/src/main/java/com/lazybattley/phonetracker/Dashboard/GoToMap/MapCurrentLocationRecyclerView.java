package com.lazybattley.phonetracker.Dashboard.GoToMap;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.CurrentLocationHelperClass;
import com.lazybattley.phonetracker.HelperClasses.MainPhoneEmailHelperClass;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;

public class MapCurrentLocationRecyclerView {
    private List<String> availableLocationEmailList;
    private List<MainPhoneEmailHelperClass> mainPhoneEmailList;
    private List<CurrentLocationHelperClass> currentLocationList;
    private MapViewCurrentLocationInterface mapViewInterface;
    private ValueEventListener locationCallback;
    private Query[] queries;
    private boolean connected;


    public MapCurrentLocationRecyclerView(MapViewCurrentLocationInterface mapViewInterface) {
        this.mapViewInterface = mapViewInterface;
    }


    public void beginSearch() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS);
        String AVAILABLE_LOCATION = "available_location";
        Query query = reference.child(ENCODED_EMAIL).child(AVAILABLE_LOCATION);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    availableLocationEmailList = new ArrayList<>();
                    for (DataSnapshot emailList : snapshot.getChildren()) {
                        availableLocationEmailList.add(emailList.getKey());
                    }
                    if(availableLocationEmailList.size() != 0){
                        setPhoneEmailList(reference, availableLocationEmailList);
                    }
                }else{
                    currentLocationList = new ArrayList<>();
                    mapViewInterface.setRecyclerViewData(currentLocationList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPhoneEmailList(DatabaseReference reference, List<String> emailList) {
        final int[] count = {0};
        mainPhoneEmailList = new ArrayList<>();
        Query query;
        for (String email : emailList) {
            query = reference.child(email).child(USER_DETAIL);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    SignUpHelperClass friendDetail = snapshot.getValue(SignUpHelperClass.class);
                    String fullName = friendDetail.getFullName();
                    String friendEmail = friendDetail.getEmail().replace('.', ',');
                    String mainPhone = friendDetail.getMainPhone();
                    boolean canTrack = friendDetail.isTraceable();
                    mainPhoneEmailList.add(new MainPhoneEmailHelperClass(fullName, friendEmail, mainPhone, canTrack));
                    count[0]++;
                    if (count[0] == emailList.size()) {
                        recyclerViewData(reference, mainPhoneEmailList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void recyclerViewData(DatabaseReference reference, List<MainPhoneEmailHelperClass> mainPhoneEmailList) {
        currentLocationList = new ArrayList<>();
        queries = new Query[mainPhoneEmailList.size()];
        for (int i = 0; i < mainPhoneEmailList.size(); i++) {
            queries[i] = reference.child(mainPhoneEmailList.get(i).getEmail())
                    .child(REGISTERED_DEVICES)
                    .child(mainPhoneEmailList.get(i).getMainPhone());
        }
        resumeCallback();
    }

    public void removeCallback(){
        if(queries.length != 0 && connected){
            for(Query query: queries){
                query.removeEventListener(locationCallback);
            }
            connected = false;
        }
    }
    
    public void resumeCallback(){
        if(queries != null && !connected){
            for(Query query: queries){
                query.addValueEventListener(locationCallback);
            }
            connected = true;
        }
    }

    public void initializeCallback() {
        locationCallback = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PhoneTrackHelperClass userLocationDetails = snapshot.getValue(PhoneTrackHelperClass.class);
                String fullName = userLocationDetails.getEmail();
                LatLng coordinates = new LatLng(userLocationDetails.getLatitude(), userLocationDetails.getLongitude());
                long updatedAt = userLocationDetails.getUpdatedAt();
                boolean traceable = userLocationDetails.isAvailable();
                if (currentLocationList.size() == mainPhoneEmailList.size()) {
                    int replaceIndex = checkIndexNumber(userLocationDetails.getEmail());
                    currentLocationList.set(replaceIndex, new CurrentLocationHelperClass(fullName, coordinates, updatedAt, traceable));
                } else {
                    currentLocationList.add(new CurrentLocationHelperClass(fullName, coordinates, updatedAt, traceable));
                }
                mapViewInterface.setRecyclerViewData(currentLocationList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    private int checkIndexNumber(String email) {
        for (int i = 0; i < mainPhoneEmailList.size(); i++) {
            if (email.equals(mainPhoneEmailList.get(i).getEmail())) {
                return i;
            }
        }
        return -1;
    }

}














