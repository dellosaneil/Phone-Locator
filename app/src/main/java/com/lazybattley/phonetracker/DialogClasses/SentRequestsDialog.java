package com.lazybattley.phonetracker.DialogClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.lazybattley.phonetracker.HelperClasses.PendingRequestHelperClass;
import com.lazybattley.phonetracker.HelperClasses.SentRequestHelperClass;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.NOTIFICATIONS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.PENDING_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.SENT_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;


public class SentRequestsDialog {

    private Activity activity;
    private String encodedFriendEmail;
    private DatabaseReference reference;
    private String encodedUserEmail;
    private String decodedFriendEmail;
    private long currentTime;
    private String decodedUserEmail;


    public SentRequestsDialog(Activity activity, String encodedFriendEmail) {
        this.activity = activity;
        this.encodedFriendEmail = encodedFriendEmail.replace('.', ',');
        this.decodedUserEmail = (FirebaseAuth.getInstance().getCurrentUser().getEmail());
        this.encodedUserEmail = decodedUserEmail.replace('.', ',');

        this.reference = FirebaseDatabase.getInstance().getReference(USERS);
        this.decodedFriendEmail = encodedFriendEmail;
        this.currentTime = System.currentTimeMillis();
        checkStatus(reference);
    }

    public void checkStatus(DatabaseReference reference) {
        Query query = reference.child(encodedUserEmail).child(SENT_REQUESTS).child(encodedFriendEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SentRequestHelperClass details = snapshot.getValue(SentRequestHelperClass.class);
                    if (details.getStatus().equals("Accepted")) {
                        acceptedRequest();
                    } else if (details.getStatus().equals("Pending")) {
                        cancelRequest();
                    } else {
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
                .setTitle(activity.getString(R.string.request_location_dialog_title_accepted, encodedFriendEmail.replace(',', '.')))
                .setMessage(activity.getString(R.string.request_location_accepted_message, encodedFriendEmail.replace(',', '.')))
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
                .setTitle(activity.getString(R.string.request_location_dialog_title_declined, encodedFriendEmail.replace(',', '.')))
                .setMessage(R.string.request_location_declined_message)
                .setPositiveButton(R.string.request_location_resend_request, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendRequest(reference);
                        requestList(reference);
                        Toast.makeText(activity, "Request Sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    private void cancelRequest() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.request_location_dialog_title))
                .setMessage(encodedFriendEmail.replace(',', '.'))
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

    private void sendRequest(DatabaseReference reference) {
        requestList(reference);
        reference = reference.child(encodedFriendEmail).child(NOTIFICATIONS).child(PENDING_REQUESTS);
        reference.child(encodedUserEmail).setValue(new PendingRequestHelperClass(decodedUserEmail, currentTime));
    }

    //records the sent notification on the current user
    private void requestList(DatabaseReference reference) {
        reference = reference.child(encodedUserEmail).child(SENT_REQUESTS).child(encodedFriendEmail);
        reference.setValue(new SentRequestHelperClass(decodedFriendEmail, currentTime, "Pending"));
    }


    private void removeRequestFromCurrentUser(DatabaseReference reference) {
        reference = reference.child(encodedUserEmail).child(SENT_REQUESTS).child(encodedFriendEmail);
        reference.removeValue();
    }

    private void removeRequestFromFriend(DatabaseReference reference) {
        reference = reference.child(encodedFriendEmail).child(NOTIFICATIONS).child(PENDING_REQUESTS).child(encodedUserEmail);
        reference.removeValue();
    }
}
