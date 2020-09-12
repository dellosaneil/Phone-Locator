package com.lazybattley.phonetracker.DialogClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.HelperClasses.RequestLocationFriendHelperClass;
import com.lazybattley.phonetracker.R;

import java.util.HashMap;
import java.util.Map;

import static com.lazybattley.phonetracker.GlobalVariables.ALL_NOTIFICATIONS;
import static com.lazybattley.phonetracker.GlobalVariables.NOTIFICATION_STATUS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_LIST_OF_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;

public class RespondRequestDialog {


    public static final String AVAILABLE_LOCATIONS = "available_location";

    private Activity activity;
    private String friendEmail;
    private DatabaseReference reference;
    private String user;

    public RespondRequestDialog(Activity activity, String friendEmail) {
        this.activity = activity;
        this.friendEmail = friendEmail.replace('.',',');
        this.user = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        this.reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE);
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
                        addedToList(reference);
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

    private void addedToList(DatabaseReference reference) {
        long time = System.currentTimeMillis();
        String decodedEmail = decodeEmail(user);
        reference = reference.child(friendEmail).child(AVAILABLE_LOCATIONS).child(user);
        reference.setValue(new RequestLocationFriendHelperClass(decodedEmail, time));
    }


    private void removeNotificationFromCurrentUser(DatabaseReference reference){
        reference = reference.child(user).child(ALL_NOTIFICATIONS).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION).child(friendEmail);
        reference.removeValue();
    }

    private void updateStatus(DatabaseReference reference, String answer){
        Map<String, Object> temp = new HashMap<>();
        temp.put(NOTIFICATION_STATUS, answer);
        reference = reference.child(friendEmail).child(REQUEST_PERMISSION_LIST_OF_REQUESTS)
                .child(user);
        reference.updateChildren(temp);
    }

    private String decodeEmail(String encodedEmail){
        return encodedEmail.replace(',', '.');
    }

}
