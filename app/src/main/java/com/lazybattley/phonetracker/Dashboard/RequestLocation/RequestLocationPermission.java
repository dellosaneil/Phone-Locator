package com.lazybattley.phonetracker.Dashboard.RequestLocation;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.DialogClasses.SentRequestsDialog;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationCurrentHelperClass;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationFriendHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.RequestLocationAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.GlobalVariables.ALL_NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_LIST_OF_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class RequestLocationPermission extends AppCompatActivity implements RequestLocationAdapter.RequestClicked {

    private TextInputLayout requestLocation_friendEmail;
    private boolean isUser;
    private RecyclerView requestLocation_sentRequests;
    private List<RequestLocationCurrentHelperClass> sentRequests;
    private String currentUser;
    private DatabaseReference reference;
    private Toast toast;
    public static final String TIME_SENT = "timeSent";
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    private ProgressBar progressBar;
    private RequestLocationAdapter adapter;
    private String friendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_location_permission);
        progressBar = findViewById(R.id.progressBar);
        requestLocation_friendEmail = findViewById(R.id.requestLocation_friendEmail);
        requestLocation_sentRequests = findViewById(R.id.requestLocation_sentRequests);
        currentUser = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE);
        initializeRecyclerView(reference);

    }

    public void locationUpdate(View view) {
        friendEmail = requestLocation_friendEmail.getEditText().getText().toString();
        String friendName = (requestLocation_friendEmail.getEditText().getText().toString()).replace('.', ',');
        checkStatus(reference, friendName);


    }


    //asks friend to get access to current location.
    private void requestLocationUpdate() {
        Long currentTime = System.currentTimeMillis();
        //converting email into String that can be used as Reference
        currentUser = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        String friendName = (requestLocation_friendEmail.getEditText().getText().toString()).replace('.', ',');

        Query query = reference.child(friendName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //checks whether user is in database
                isUser = snapshot.exists();
                if (isUser) {
                    sendRequest(currentUser, currentTime, friendName, reference);
                    requestLocation_friendEmail.setError(null);
                    requestLocation_friendEmail.setErrorEnabled(false);
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(RequestLocationPermission.this, "Request Sent", Toast.LENGTH_SHORT);
                    toast.show();
                    requestLocation_friendEmail.getEditText().setText("");
                } else {
                    requestLocation_friendEmail.setError("User does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkStatus(DatabaseReference reference, String decodedFriendEmail) {
        if (checkInput(friendEmail)) {
            Query query = reference.child(currentUser).child(REQUEST_PERMISSION_LIST_OF_REQUESTS).child(decodedFriendEmail);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        RequestLocationCurrentHelperClass details = snapshot.getValue(RequestLocationCurrentHelperClass.class);
                        if (details.getStatus().equals("Accepted")) {
                            Toast.makeText(RequestLocationPermission.this, "User already accepted request.", Toast.LENGTH_SHORT).show();
                            requestLocation_friendEmail.getEditText().setText("");
                        }
                    } else {
                        requestLocationUpdate();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }


    //checks if user input an email
    private boolean checkInput(String friendName) {
        if (friendName.length() == 0) {
            requestLocation_friendEmail.setError(getText(R.string.missing_field));
            return false;
        }
        return true;
    }

    //send the request notification to the friend
    private void sendRequest(String currentUser, Long time, String friendName, DatabaseReference reference) {
        requestList(currentUser, friendName, reference, time);
        String originalEmail = currentUser.replace(',', '.');
        reference = reference.child(friendName).child(ALL_NOTIFICATIONS).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION);
        reference.child(currentUser).setValue(new RequestLocationFriendHelperClass(originalEmail, time));
    }

    //records the sent notification on the current user
    private void requestList(String currentUser, String friendName, DatabaseReference reference, Long time) {
        String email = friendName.replace(',', '.');
        reference = reference.child(currentUser).child(REQUEST_PERMISSION_LIST_OF_REQUESTS);
        reference.child(friendName).setValue(new RequestLocationCurrentHelperClass(email, time, "Pending"));
    }

    private void initializeRecyclerView(DatabaseReference reference) {
        Query query = reference.child(currentUser).child(REQUEST_PERMISSION_LIST_OF_REQUESTS).orderByChild(TIME_SENT);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sentRequests = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot requests : snapshot.getChildren()) {
                        sentRequests.add(requests.getValue(RequestLocationCurrentHelperClass.class));
                    }
                }
                adapter = new RequestLocationAdapter(RequestLocationPermission.this, sentRequests, progressBar, RequestLocationPermission.this);
                requestLocation_sentRequests.setAdapter(adapter);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
                requestLocation_sentRequests.setLayoutManager(linearLayoutManager);
                DividerItemDecoration item = new DividerItemDecoration(RequestLocationPermission.this,
                        linearLayoutManager.getOrientation());
                requestLocation_sentRequests.addItemDecoration(item);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void requestClicked(int position) {
        new SentRequestsDialog(this, sentRequests.get(position).getEmail());
    }
}