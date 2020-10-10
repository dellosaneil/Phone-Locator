package com.lazybattley.phonetracker.DialogClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.HelperClasses.AcceptedUsersHelperClass;
import com.lazybattley.phonetracker.HelperClasses.AvailableLocationHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SignUpHelperClass;
import com.lazybattley.phonetracker.R;

import java.util.HashMap;
import java.util.Map;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.NOTIFICATIONS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.PENDING_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.SENT_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.STATUS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;


public class RespondRequestDialog {


    public static final String AVAILABLE_LOCATIONS = "available_location";
    public static final String ACCEPTED_USERS = "accepted_users";
    public static final String USER_DETAILS = "user_detail";

    private Activity activity;
    private String encodedFriendEmail;
    private DatabaseReference reference;
    private String phoneOwnerFullName;
    private String friendFullName = null;

    public RespondRequestDialog(Activity activity, String encodedFriendEmail, String phoneOwnerFullName) {
        this.phoneOwnerFullName = phoneOwnerFullName;
        this.activity = activity;
        this.encodedFriendEmail = encodedFriendEmail.replace('.', ',');
        this.reference = FirebaseDatabase.getInstance().getReference(USERS);
        setFriendFullName(reference);
        createAlertDialog();
    }

    public void createAlertDialog() {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.notification_dialog_message)
                .setPositiveButton(R.string.notification_dialog_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateStatus(reference, "Accepted");
                        removeNotificationFromCurrentUser(reference);
                        acceptedRequestFriend(reference);
                        acceptedRequestUser(reference);
                    }
                })
                .setNegativeButton(R.string.notification_dialog_decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateStatus(reference, "Declined");
                        removeNotificationFromCurrentUser(reference);
                    }
                })
                .show();
    }

    private void acceptedRequestFriend(final DatabaseReference reference) {
        long time = System.currentTimeMillis();
        String[] mainPhone = new String[1];
        DatabaseReference query = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(USER_DETAILS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpHelperClass details = snapshot.getValue(SignUpHelperClass.class);
                mainPhone[0] = details.getMainPhone();
                reference.child(encodedFriendEmail).child(AVAILABLE_LOCATIONS).child(ENCODED_EMAIL)
                        .setValue(new AvailableLocationHelperClass(phoneOwnerFullName, ENCODED_EMAIL, time, mainPhone[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setFriendFullName(DatabaseReference reference) {
        Query query = reference.child(encodedFriendEmail).child(USER_DETAILS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SignUpHelperClass userDetails = snapshot.getValue(SignUpHelperClass.class);
                friendFullName = userDetails.getFullName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //clears the notification from the user dashboard
    private void removeNotificationFromCurrentUser(DatabaseReference reference) {
        reference = reference.child(ENCODED_EMAIL).child(NOTIFICATIONS).child(PENDING_REQUESTS).child(encodedFriendEmail);
        reference.removeValue();
    }

    //reflected on friend user table. "accepted or declined"
    private void updateStatus(DatabaseReference reference, String answer) {
        Map<String, Object> temp = new HashMap<>();
        temp.put(STATUS, answer);
        reference = reference.child(encodedFriendEmail).child(SENT_REQUESTS)
                .child(ENCODED_EMAIL);
        reference.updateChildren(temp);
    }


    //reflected on current user table.
    private void acceptedRequestUser(DatabaseReference reference) {
        reference = reference.child(ENCODED_EMAIL).child(ACCEPTED_USERS).child(encodedFriendEmail);
        if(friendFullName == null){
            DatabaseReference tempReference = FirebaseDatabase.getInstance().getReference(USERS);
            acceptedRequestUser(tempReference);
        }else{
            reference.setValue(new AcceptedUsersHelperClass(friendFullName, encodedFriendEmail.replace(',', '.'), System.currentTimeMillis()));
        }
    }
}
