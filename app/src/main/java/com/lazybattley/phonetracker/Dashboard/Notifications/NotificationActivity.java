package com.lazybattley.phonetracker.Dashboard.Notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationFriendHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.GlobalVariables.ALL_NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class NotificationActivity extends AppCompatActivity {

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
        NotificationAdapter adapter = new NotificationAdapter(this, messages);
        notification_recyclerView.setAdapter(adapter);
        notification_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration item = new DividerItemDecoration(notification_recyclerView.getContext(), layoutManager.getOrientation());
        notification_recyclerView.addItemDecoration(item);
    }

    private void requests() {
        receivedRequests = new ArrayList<>();
        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace('.', ',');
        reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE).child(user).child(ALL_NOTIFICATIONS).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

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


}