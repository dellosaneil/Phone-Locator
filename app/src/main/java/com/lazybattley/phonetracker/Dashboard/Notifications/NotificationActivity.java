package com.lazybattley.phonetracker.Dashboard.Notifications;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lazybattley.phonetracker.DialogClasses.RespondRequestDialog;
import com.lazybattley.phonetracker.HelperClasses.PendingRequestHelperClass;
import com.lazybattley.phonetracker.R;
import com.lazybattley.phonetracker.RecyclerViewAdapters.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.ENCODED_EMAIL;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.NOTIFICATIONS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.PENDING_REQUESTS;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.TIME_SENT;
import static com.lazybattley.phonetracker.Dashboard.MainDashBoardActivity.USERS;


public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.NotificationClick, NotificationFinishedInitialization {

    private RecyclerView notification_recyclerView;
    private LinearLayoutManager layoutManager;
    private List<PendingRequestHelperClass> requests;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        notification_recyclerView = findViewById(R.id.notification_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        initializeRecyclerView();

        NotificationBackgroundThread notificationThread = new NotificationBackgroundThread(this, executorService, this);
        notificationThread.executeThread();
    }

    private void initializeRecyclerView() {
        adapter = new NotificationAdapter(this);
        notification_recyclerView.setAdapter(adapter);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        notification_recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration item = new DividerItemDecoration(notification_recyclerView.getContext(), layoutManager.getOrientation());
        notification_recyclerView.addItemDecoration(item);
    }

    @Override
    public void onClickNotification(int position) {
        new RespondRequestDialog(this, requests.get(position).getEmail());
    }

    @Override
    public void displayNotification(List<PendingRequestHelperClass> receivedRequests) {
        requests = receivedRequests;
        adapter.setRequests(requests);
    }

    private static class NotificationBackgroundThread implements Runnable {
        private List<PendingRequestHelperClass> receivedRequests;
        private Context context;
        private Executor executor;
        private NotificationFinishedInitialization callback;

        public NotificationBackgroundThread(Context context, Executor executor, NotificationFinishedInitialization callback) {
            this.context = context;
            this.executor = executor;
            this.receivedRequests = new ArrayList<>();
            this.callback = callback;
        }

        private void requests() {
            //Populate the RecyclerView with location request notifications.
            Query query = FirebaseDatabase.getInstance().getReference(USERS).child(ENCODED_EMAIL).child(NOTIFICATIONS).child(PENDING_REQUESTS).orderByChild(TIME_SENT);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    receivedRequests = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot requests : snapshot.getChildren()) {
                            receivedRequests.add(requests.getValue(PendingRequestHelperClass.class));
                        }
                    } else {
                        Toast.makeText(context, "No notification", Toast.LENGTH_SHORT).show();
                    }
                    callback.displayNotification(receivedRequests);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public void run() {
            requests();
        }

        public void executeThread() {
            executor.execute(this);
        }
    }
}

interface NotificationFinishedInitialization {
    void displayNotification(List<PendingRequestHelperClass> receivedRequests);
}


























