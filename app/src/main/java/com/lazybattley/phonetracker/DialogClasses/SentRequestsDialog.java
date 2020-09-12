package com.lazybattley.phonetracker.DialogClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.Dashboard.GoToMap.MapCurrentLocationActivity;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationCurrentHelperClass;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationFriendHelperClass;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.ALL_NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_LIST_OF_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;


public class SentRequestsDialog {

    private Activity activity;
    private String friendEmail;
    private DatabaseReference reference;
    private String user;


    public SentRequestsDialog(Activity activity, String friendEmail) {
        this.activity = activity;
        this.friendEmail = friendEmail.replace('.', ',');
        this.user = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        this.reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE);
        checkStatus(reference);
    }

    public void checkStatus(DatabaseReference reference) {
        Query query = reference.child(user).child(REQUEST_PERMISSION_LIST_OF_REQUESTS).child(friendEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RequestLocationCurrentHelperClass details = snapshot.getValue(RequestLocationCurrentHelperClass.class);
                    if(details.getStatus().equals("Accepted")){
                        acceptedRequest();
                    }else{
                        declinedRequest();
                    }
                } else {
                    cancelRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void acceptedRequest() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.request_location_dialog_title_accepted, friendEmail.replace(',', '.')))
                .setMessage(activity.getString(R.string.request_location_accepted_message, friendEmail.replace(',', '.')))
                .setPositiveButton(R.string.request_location_accepted_go_to_map, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.startActivity(new Intent(activity, MapCurrentLocationActivity.class));
                    }
                })
                .show();
    }


    private void declinedRequest() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.request_location_dialog_title_declined, friendEmail.replace(',', '.')))
                .setMessage(R.string.request_location_declined_message)
                .setPositiveButton(R.string.request_location_resend_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendRequest(user, System.currentTimeMillis(), friendEmail, reference);
                        Toast.makeText(activity, "Request Sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

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


    private void cancelRequest() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.request_location_dialog_title))
                .setMessage(friendEmail.replace(',', '.'))
                .setPositiveButton(activity.getText(R.string.request_location_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRequestFromCurrentUser(reference);
                        removeRequestFromFriend(reference);
                    }
                })
                .setNegativeButton(activity.getText(R.string.request_location_cancel), null)
                .show();
    }

    private void removeRequestFromCurrentUser(DatabaseReference reference) {
        reference = reference.child(user).child(REQUEST_PERMISSION_LIST_OF_REQUESTS).child(friendEmail);
        reference.removeValue();
    }

    private void removeRequestFromFriend(DatabaseReference reference) {
        reference = reference.child(friendEmail).child(ALL_NOTIFICATIONS).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION).child(user);
        reference.removeValue();

    }


}
