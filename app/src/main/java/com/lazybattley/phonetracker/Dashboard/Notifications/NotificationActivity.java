package com.lazybattley.phonetracker.Dashboard.Notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.DialogClasses.RespondRequestDialog;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationFriendHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.Dashboard.RequestLocation.RequestLocationPermission.TIME_SENT;
import static com.lazybattley.phonetracker.GlobalVariables.ALL_NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.NotificationClick {

    private RecyclerView notification_recyclerView;
    private DatabaseReference reference;
    private LinearLayoutManager layoutManager;
    private List<RequestLocationFriendHelperClass> receivedRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        notification_recyclerView = findViewById(R.id.notification_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        requests();
    }

    private void initializeRecyclerView(List<RequestLocationFriendHelperClass> messages) {
        NotificationAdapter adapter = new NotificationAdapter(this, messages,this);
        notification_recyclerView.setAdapter(adapter);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        notification_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration item = new DividerItemDecoration(notification_recyclerView.getContext(), layoutManager.getOrientation());
        notification_recyclerView.addItemDecoration(item);
    }

    private void requests() {
        //Populate the RecyclerView with location request notifications.
        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace('.', ',');
        Query query = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user).child(ALL_NOTIFICATIONS).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION).orderByChild(TIME_SENT);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receivedRequests = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        receivedRequests.add(snap.getValue(RequestLocationFriendHelperClass.class));
                    }
                } else {
                    Toast.makeText(NotificationActivity.this, "No notification", Toast.LENGTH_SHORT).show();
                }
                initializeRecyclerView(receivedRequests);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onClickNotification(int position) {
        new RespondRequestDialog(this, receivedRequests.get(position).getEmail());
    }
}