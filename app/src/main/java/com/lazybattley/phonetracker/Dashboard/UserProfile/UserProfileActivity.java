package com.lazybattley.phonetracker.Dashboard.UserProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.AcceptedUsersHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.UserProfileAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.TIME_SENT;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;
import static com.lazybattley.phonetracker.DialogClasses.RespondRequestDialog.ACCEPTED_USERS;

public class UserProfileActivity extends AppCompatActivity {

    private RecyclerView userProfile_recyclerView;
    private List<String> permittedUsers;
    private UserProfileAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        layoutManager = new LinearLayoutManager(this);
        userProfile_recyclerView = findViewById(R.id.userProfile_recyclerView);
        adapter = new UserProfileAdapter();
        getPermittedUsers();
        userProfile_recyclerView.setAdapter(adapter);
        userProfile_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        userProfile_recyclerView.addItemDecoration(decoration);
    }

    private void getPermittedUsers(){
        Query query = FirebaseDatabase.getInstance().getReference(USERS)
                .child(ENCODED_EMAIL).child(ACCEPTED_USERS).orderByChild(TIME_SENT);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                permittedUsers = new ArrayList<>();
                if(snapshot.exists()){
                    for(DataSnapshot users: snapshot.getChildren()){
                        AcceptedUsersHelperClass user = users.getValue(AcceptedUsersHelperClass.class);
                        permittedUsers.add(user.getFullName());
                    }
                    adapter.setPermittedPeople(permittedUsers);
                }else{
                    Toast.makeText(UserProfileActivity.this, "No Permitted Users.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}