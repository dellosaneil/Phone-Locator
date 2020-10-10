package com.lazybattley.phonetracker.Dashboard.Notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.DialogClasses.RespondRequestDialog;
import com.lazybattley.phonetracker.HelperClasses.PendingRequestHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.NOTIFICATIONS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.PENDING_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.TIME_SENT;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;


public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.NotificationClick, NotificationFinishedInitialization {

    private RecyclerView notification_recyclerView;
    private LinearLayoutManager layoutManager;
    private List<PendingRequestHelperClass> requests;
    private NotificationAdapter adapter;
    private static final String TAG = "NotificationActivity";
    private NotificationBackgroundThread notificationThread;
    private String phoneOwnerFullName;
    private boolean isLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setPhoneOwnerFullName();
        notification_recyclerView = findViewById(R.id.notification_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        initializeRecyclerView();
        notificationThread = new NotificationBackgroundThread(this,this);
    }

    private void setPhoneOwnerFullName(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String preferenceEmail = preferences.getString("SIGN_UP_EMAIL", "noEmail");
        if(preferenceEmail.equals(ENCODED_EMAIL)){
            phoneOwnerFullName = preferences.getString("FULL_NAME", null);
            isLoading = false;
        }else{
            Query query = FirebaseDatabase.getInstance().getReference(USERS)
                    .child(ENCODED_EMAIL).child(USER_DETAIL);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        SignUpHelperClass name = snapshot.getValue(SignUpHelperClass.class);
                        phoneOwnerFullName = name.getFullName();
                        SharedPreferences.Editor saveEmail = preferences.edit();
                        saveEmail.putString("SIGN_UP_EMAIL", ENCODED_EMAIL);
                        saveEmail.putString("FULL_NAME", phoneOwnerFullName);
                        saveEmail.apply();
                        isLoading = false;
                    }else{
                        Toast.makeText(NotificationActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        notificationThread.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationThread.attachListener();
    }

    private void initializeRecyclerView() {
        adapter = new NotificationAdapter(this);
        notification_recyclerView.setAdapter(adapter);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        notification_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration item = new DividerItemDecoration(notification_recyclerView.getContext(), layoutManager.getOrientation());
        notification_recyclerView.addItemDecoration(item);
    }

    @Override
    public void onClickNotification(int position) {
        if(!isLoading){
            new RespondRequestDialog(this, requests.get(position).getEmail(), phoneOwnerFullName);
        }else{
            Toast.makeText(this, "Loading data... Press button again", Toast.LENGTH_SHORT).show();
        }
        
    }

    @Override
    public void displayNotification(List<PendingRequestHelperClass> receivedRequests) {
        requests = receivedRequests;
        adapter.setRequests(requests);
    }

    private static class NotificationBackgroundThread{
        private List<PendingRequestHelperClass> receivedRequests;
        private Context context;
        private NotificationFinishedInitialization callback;
        private ValueEventListener requestListener;
        private Query requestQuery;

        public NotificationBackgroundThread(Context context, NotificationFinishedInitialization callback) {
            this.context = context;
            this.receivedRequests = new ArrayList<>();
            this.callback = callback;
            initializeRequestQuery();
            requestQuery = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(NOTIFICATIONS).child(PENDING_REQUESTS).orderByChild(TIME_SENT);
        }

        private void attachListener() {
            //Populate the RecyclerView with location request notifications.
            requestQuery.addValueEventListener(requestListener);
        }

        private void detachListener(){
            requestQuery.removeEventListener(requestListener);
        }

        private void initializeRequestQuery(){
            requestListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    receivedRequests = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot requests : snapshot.getChildren()) {
                            receivedRequests.add(requests.getValue(PendingRequestHelperClass.class));
                        }
                    } else {
                        Toast.makeText(context, "No notification", Toast.LENGTH_SHORT).show();
                    }
                    callback.displayNotification(receivedRequests);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
        }
    }
}

interface NotificationFinishedInitialization {
    void displayNotification(List<PendingRequestHelperClass> receivedRequests);
}
