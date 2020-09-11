package com.lazybattley.phonetracker.DialogClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_LIST_OF_REQUESTS;
import static com.lazybattley.phonetracker.GlobalVariables.REQUEST_PERMISSION_TO_ACCESS_LOCATION;
import static com.lazybattley.phonetracker.GlobalVariables.USERS_REFERENCE;


public class SentRequestsDialog {

    private Activity activity;
    private String friendEmail;
    private  DatabaseReference reference;
    private String user;


    public SentRequestsDialog(Activity activity, String friendEmail) {
        this.activity = activity;
        this.friendEmail = friendEmail.replace('.',',');
        this.user = (FirebaseAuth.getInstance().getCurrentUser().getEmail()).replace('.', ',');
        this.reference = FirebaseDatabase.getInstance().getReference(USERS_REFERENCE);
        createAlertDialog();

    }

    public void createAlertDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.request_location_dialog_title))
                .setMessage(friendEmail.replace(',','.'))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRequestFromCurrentUser(reference);
                        removeRequestFromFriend(reference);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeRequestFromCurrentUser(DatabaseReference reference) {
        reference = reference.child(user).child(REQUEST_PERMISSION_LIST_OF_REQUESTS).child(friendEmail);
        reference.removeValue();
    }

    private void removeRequestFromFriend(DatabaseReference reference){
        reference = reference.child(friendEmail).child(REQUEST_PERMISSION_TO_ACCESS_LOCATION).child(user);
        reference.removeValue();

    }


}
