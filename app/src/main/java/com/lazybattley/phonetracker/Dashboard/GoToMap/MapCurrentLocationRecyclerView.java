package com.lazybattley.phonetracker.Dashboard.GoToMap;

import android.util.Log;

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
    private ValueEventListener locationCallback, availableLocationCallback, availabilityCallback;
    private DatabaseReference ref;
    private Query[] queries, availabilityQuery;
    private Query availableLocationQuery;
    private static final String TAG = "MapCurrentLocationRecyc";


    public MapCurrentLocationRecyclerView(MapViewCurrentLocationInterface mapViewInterface) {
        this.mapViewInterface = mapViewInterface;
        setAvailableLocationCallback();
        initializeAvailabilityCallback();
        initializeCallback();
    }

    public void beginInitializeRecyclerViewData() {
        String AVAILABLE_LOCATION = "available_location";
        availableLocationQuery = ref.child(ENCODED_EMAIL).child(AVAILABLE_LOCATION);
        availableLocationQuery.addValueEventListener(availableLocationCallback);
    }

    private void setAvailableLocationCallback(){
        ref = FirebaseDatabase.getInstance().getReference(USERS);
        availableLocationCallback = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                removeAvailabilityChecker();
                if (snapshot.exists()) {
                    availableLocationEmailList = new ArrayList<>();
                    for (DataSnapshot emailList : snapshot.getChildren()) {
                        availableLocationEmailList.add(emailList.getKey());
                    }
                    if (availableLocationEmailList.size() != 0) {
                        setPhoneEmailList(ref, availableLocationEmailList);
                    }
                } else {
                    currentLocationList = new ArrayList<>();
                    mapViewInterface.setRecyclerViewData(currentLocationList);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }

    public void removeAvailableLocationCallback(){
        if(availableLocationQuery != null){
            availableLocationQuery.removeEventListener(availableLocationCallback);
        }
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
        singleValueCallback();
        updateAvailability(availableLocationEmailList);
    }

    public void singleValueCallback() {
        if (queries != null) {
            for (Query query : queries) {
                query.addListenerForSingleValueEvent(locationCallback);
            }
        }
    }

    public void removeTracking(int previousPosition) {
        if (queries.length != 0) {
            queries[previousPosition].removeEventListener(locationCallback);
        }
    }

    public void trackDevice(int position) {
        if (queries != null) {
            queries[position].addValueEventListener(locationCallback);
        }
    }

    public void removeAvailabilityChecker(){
        if(availabilityQuery != null){
            for(Query q : availabilityQuery){
                q.removeEventListener(availabilityCallback);
            }
        }
    }

    public void attachAvailabilityChecker(){
        if(availabilityQuery != null){
            for(Query query: availabilityQuery){
                query.addValueEventListener(availabilityCallback);
            }
        }
    }


    // Updates the tracker information of a User
    private void updateAvailability(List<String> emails) {
        availabilityQuery = new Query[emails.size()];
        for (int i = 0; i < emails.size(); i++) {
            availabilityQuery[i] = FirebaseDatabase.getInstance().getReference(USERS)
                    .child(emails.get(i)).child(USER_DETAIL);
            availabilityQuery[i].addValueEventListener(availabilityCallback);
        }
    }

    private void initializeAvailabilityCallback() {
        availabilityCallback = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SignUpHelperClass userDetails = snapshot.getValue(SignUpHelperClass.class);
                    boolean status = userDetails.isTraceable();
                    String emailAddress = userDetails.getEmail().replace('.',',');
                    int indexNumber = checkIndexNumber(emailAddress);
                    currentLocationList.get(indexNumber).setTraceable(status);
                    mapViewInterface.setRecyclerViewData(currentLocationList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }


    //Method to update location of Friend User
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














