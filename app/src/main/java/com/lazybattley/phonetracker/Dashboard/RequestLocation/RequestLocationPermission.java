package com.lazybattley.phonetracker.Dashboard.RequestLocation;

import android.os.Bundle;
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
import com.lazybattley.phonetracker.HelperClasses.SentRequestHelperClass;
import com.lazybattley.phonetracker.HelperClasses.PendingRequestHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.RequestLocationAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.GlobalVariables.NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.SENT_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.PENDING_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;

public class RequestLocationPermission extends AppCompatActivity implements RequestLocationAdapter.RequestClicked {

    private TextInputLayout requestLocation_friendEmail;
    private boolean isUser;
    private RecyclerView requestLocation_sentRequests;
    private List<SentRequestHelperClass> sentRequests;
    private String encodedUserEmail;
    private DatabaseReference reference;
    private Toast toast;
    private final String TIME_SENT = "timeSent";
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    private ProgressBar progressBar;
    private RequestLocationAdapter adapter;
    private String decodedFriendEmail;
    private String encodedFriendEmail;
    private String decodedUserEmail;
    private long currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_location_permission);
        progressBar = findViewById(R.id.progressBar);
        requestLocation_friendEmail = findViewById(R.id.requestLocation_friendEmail);
        requestLocation_sentRequests = findViewById(R.id.requestLocation_sentRequests);
        encodedUserEmail = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        decodedUserEmail = encodedUserEmail.replace(',', '.');
        reference = FirebaseDatabase.getInstance().getReference(USERS);
        initializeRecyclerView(reference);

    }

    public void locationUpdate(View view) {
        decodedFriendEmail = requestLocation_friendEmail.getEditText().getText().toString();
        encodedFriendEmail = decodedFriendEmail.replace('.', ',');
        checkStatus(reference);
    }


    //asks friend to get access to current location.
    private void requestLocationUpdate() {
        currentTime = System.currentTimeMillis();
        //converting email into String that can be used as Reference
        if (!encodedFriendEmail.equals(encodedUserEmail)) {
            Query query = reference.child(encodedFriendEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //checks whether user is in database
                    isUser = snapshot.exists();
                    if (isUser) {
                        sendRequest(reference);
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
        } else {
            Toast.makeText(this, "Cannot request for own account", Toast.LENGTH_SHORT).show();
            requestLocation_friendEmail.getEditText().setText("");
        }


    }

    private void checkStatus(DatabaseReference reference) {
        if (checkInput(decodedFriendEmail)) {
            Query query = reference.child(encodedUserEmail).child(SENT_REQUESTS).child(encodedFriendEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        SentRequestHelperClass details = snapshot.getValue(SentRequestHelperClass.class);
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
    private void sendRequest(DatabaseReference reference) {
        requestList(reference);
        reference = reference.child(encodedFriendEmail).child(NOTIFICATIONS).child(PENDING_REQUESTS);
        reference.child(encodedUserEmail).setValue(new PendingRequestHelperClass(decodedUserEmail, currentTime));
    }

    //records the sent notification on the current user
    private void requestList(DatabaseReference reference) {
        reference = reference.child(encodedUserEmail).child(SENT_REQUESTS);
        reference.child(encodedFriendEmail).setValue(new SentRequestHelperClass(decodedFriendEmail, currentTime, "Pending"));
    }

    private void initializeRecyclerView(DatabaseReference reference) {
        new Thread(() -> {
            Query query = reference.child(encodedUserEmail).child(SENT_REQUESTS).orderByChild(TIME_SENT);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    sentRequests = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot requests : snapshot.getChildren()) {
                            sentRequests.add(requests.getValue(SentRequestHelperClass.class));
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
        }).start();
    }


    @Override
    public void requestClicked(int position) {
        new SentRequestsDialog(this, sentRequests.get(position).getEmail());
    }
}