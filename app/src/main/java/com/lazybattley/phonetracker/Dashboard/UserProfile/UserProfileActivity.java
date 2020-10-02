package com.lazybattley.phonetracker.Dashboard.UserProfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userProfile_mainDeviceName = findViewById(R.id.userProfile_mainDeviceName);
        layoutManager = new LinearLayoutManager(this);
        userProfile_recyclerView = findViewById(R.id.userProfile_recyclerView);
        adapter = new UserProfileAdapter(this);
        getPermittedUsers();
        userProfile_recyclerView.setAdapter(adapter);
        userProfile_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        userProfile_recyclerView.addItemDecoration(decoration);
        getMainDeviceName();
    }

    private void getPermittedUsers() {
        Query query = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(ACCEPTED_USERS).orderByChild(TIME_SENT);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullNameEmail = new ArrayList<>();
                permittedUsers = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot users : snapshot.getChildren()) {
                        AcceptedUsersHelperClass user = users.getValue(AcceptedUsersHelperClass.class);
                        permittedUsers.add(user.getFullName());
                        fullNameEmail.add(new FullNameEmailHelperClass(user.getFullName(), user.getEmail()));
                    }
                    adapter.setPermittedPeople(permittedUsers);
                } else {
                    Toast.makeText(UserProfileActivity.this, "No Permitted Users.", Toast.LENGTH_SHORT).show();
                    adapter.setPermittedPeople(permittedUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMainDeviceName() {
        Query query = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(USER_DETAIL);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                                Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Toast.makeText(UserProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
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

    private void removeFromAcceptedList(int position){
        String email = fullNameEmail.get(position).getEmail().replace('.', ',');
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(ACCEPTED_USERS);
        reference.removeValue();
        removeFromAvailableLocation(email);
        updateSentRequest(email);

    }

    private void removeFromAvailableLocation(String email){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(AVAILABLE_LOCATIONS).child(ENCODED_EMAIL);
        reference.removeValue();
    }

    private void updateSentRequest(String email){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS)
                .child(email).child(SENT_REQUESTS).child(ENCODED_EMAIL);
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put(STATUS, "Removed");
        reference.updateChildren(updateStatus);


    }

}