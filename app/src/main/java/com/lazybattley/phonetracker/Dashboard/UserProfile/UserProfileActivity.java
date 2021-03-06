package com.lazybattley.phonetracker.Dashboard.UserProfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.AcceptedUsersHelperClass;
import com.lazybattley.phonetracker.HelperClasses.FullNameEmailHelperClass;
import com.lazybattley.phonetracker.HelperClasses.PhoneTrackHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.UserProfileAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.SENT_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.STATUS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.TIME_SENT;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USER_DETAIL;
import static com.lazybattley.phonetracker.DialogClasses.RespondRequestDialog.ACCEPTED_USERS;
import static com.lazybattley.phonetracker.DialogClasses.RespondRequestDialog.AVAILABLE_LOCATIONS;

public class UserProfileActivity extends AppCompatActivity implements UserProfileAdapter.OnPersonClicked {

    private RecyclerView userProfile_recyclerView;
    private List<String> permittedUsers;
    private List<FullNameEmailHelperClass> fullNameEmail;
    private UserProfileAdapter adapter;
    private LinearLayoutManager layoutManager;
    private TextView userProfile_mainDeviceName;
    private ValueEventListener permittedUsersCallback;
    private Query permittedUsersQuery;
    private ProgressBar userProfile_progressBarRecycler, userProfile_progressBarMainDevice;
    private static final String TAG = "UserProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getMainDeviceName();
        userProfile_progressBarRecycler = findViewById(R.id.userProfile_progressBarRecycler);
        userProfile_progressBarMainDevice = findViewById(R.id.userProfile_progressBarMainDevice);
        userProfile_mainDeviceName = findViewById(R.id.userProfile_mainDeviceName);
        layoutManager = new LinearLayoutManager(this);
        userProfile_recyclerView = findViewById(R.id.userProfile_recyclerView);
        adapter = new UserProfileAdapter(this);
        userProfile_recyclerView.setAdapter(adapter);
        userProfile_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        userProfile_recyclerView.addItemDecoration(decoration);
        initializePermittedUsersCallback();
    }

    private void getPermittedUsers() {
        permittedUsersQuery = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(ACCEPTED_USERS).orderByChild(TIME_SENT);
        permittedUsersQuery.addValueEventListener(permittedUsersCallback);
    }

    private void removePermittedUserCallback() {
        permittedUsersQuery.removeEventListener(permittedUsersCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        removePermittedUserCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        getPermittedUsers();
    }

    public void backButtonPressed(View view){
        onBackPressed();
    }


    private void initializePermittedUsersCallback() {
        permittedUsersCallback = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "Permitted Users Check");
                userProfile_progressBarRecycler.setVisibility(View.VISIBLE);
                fullNameEmail = new ArrayList<>();
                permittedUsers = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot users : snapshot.getChildren()) {
                        AcceptedUsersHelperClass user = users.getValue(AcceptedUsersHelperClass.class);
                        permittedUsers.add(user.getFullName());
                        fullNameEmail.add(new FullNameEmailHelperClass(user.getFullName(), user.getEmail()));
                    }
                } else {
                    Toast.makeText(UserProfileActivity.this, "No Permitted Users.", Toast.LENGTH_SHORT).show();
                }
                userProfile_progressBarRecycler.setVisibility(View.INVISIBLE);
                adapter.setPermittedPeople(permittedUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }


    private void getMainDeviceName() {
        Query mainDeviceQuery = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(USER_DETAIL);
        mainDeviceQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userProfile_progressBarMainDevice.setVisibility(View.VISIBLE);
                if (snapshot.exists()) {
                    SignUpHelperClass helper = snapshot.getValue(SignUpHelperClass.class);
                    Query q1 = FirebaseDatabase.getInstance().getReference(USERS)
                            .child(ENCODED_EMAIL).child(REGISTERED_DEVICES)
                            .child(helper.getMainPhone());
                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                PhoneTrackHelperClass name = snapshot.getValue(PhoneTrackHelperClass.class);
                                userProfile_mainDeviceName.setText(name.getDeviceName());
                            } else {
                                Toast.makeText(UserProfileActivity.this, "No Main Device Found", Toast.LENGTH_SHORT).show();
                            }
                            userProfile_progressBarMainDevice.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(UserProfileActivity.this, "No Main Device Found", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void removeFromList(int position) {
        String user = permittedUsers.get(position);
        String title = getString(R.string.user_profile_remove_title, user);
        String message = getString(R.string.user_profile_remove_message, user);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.user_profile_remove_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeFromAcceptedList(position);
                    }
                })
                .setNegativeButton(R.string.user_profile_remove_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void removeFromAcceptedList(int position) {
        String email = fullNameEmail.get(position).getEmail().replace('.', ',');
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(ACCEPTED_USERS);
        reference.removeValue();
        removeFromAvailableLocation(email);
        updateSentRequest(email);

    }

    private void removeFromAvailableLocation(String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(AVAILABLE_LOCATIONS).child(ENCODED_EMAIL);
        reference.removeValue();
    }

    private void updateSentRequest(String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(SENT_REQUESTS).child(ENCODED_EMAIL);
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(STATUS, "Removed");
        updateStatus.put(TIME_SENT, System.currentTimeMillis());
        reference.updateChildren(updateStatus);


    }

}