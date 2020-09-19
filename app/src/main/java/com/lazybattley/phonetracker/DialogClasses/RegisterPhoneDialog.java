package com.lazybattley.phonetracker.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lazybattley.phonetracker.R;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.GlobalVariables.REGISTERED_DEVICES;
import static com.lazybattley.phonetracker.GlobalVariables.USERS;

public class RegisterPhoneDialog extends AppCompatDialogFragment {

    private TextInputLayout dialogRegisterPhone_phoneName;
    private PhoneDialogListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return createDialog();
    }

    public Dialog createDialog() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_register_phone, null);
        dialogRegisterPhone_phoneName = view.findViewById(R.id.dialogRegisterPhone_phoneName);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.dialog_register_phone_register_device)
                .setPositiveButton(R.string.dialog_register_phone_positive, null) //Set to null. We override the onclick
                .setNegativeButton(R.string.dialog_register_phone_negative, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (validateRegisteredPhone() != null) {
                            listener.phoneName(validateRegisteredPhone());
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private String validateRegisteredPhone() {
        String phoneName = dialogRegisterPhone_phoneName.getEditText().getText().toString().trim();
        if (phoneName.length() == 0) {
            dialogRegisterPhone_phoneName.setError("Required Field");
            return null;
        } else if (phoneName.length() < 5) {
            dialogRegisterPhone_phoneName.setError("Length should be more than 5");
            return null;
        }else {
            dialogRegisterPhone_phoneName.setError(null);
            dialogRegisterPhone_phoneName.setErrorEnabled(false);
            return phoneName;
        }

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (PhoneDialogListener) context;
    }

    public interface PhoneDialogListener {
        void phoneName(String phone);
    }
}
