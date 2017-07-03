package com.personalspace.triggerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserSelectActivity extends AppCompatActivity implements RemoteSession.RemoteSessionDelegate{

    RemoteSession session = null;
    String sessionName = null;
    ProgressDialog dialog;

    ArrayAdapter<String> listAdapter = null;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_user_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //request remote session start
        sessionName = getIntent().getStringExtra("sessionName");
        session = RemoteSession.getInstance(sessionName);

        Thread workerThread = new Thread(session);
        workerThread.start();

        session.getAllUsers(this);

        //show the progress dialog
        dialog.setMessage("Retrieving Users. Please Wait...");
        dialog.setCancelable(false);
        dialog.show();
    }

    public void onSubmit(View view){

        // Get selected participants
        ListView participant1View = (ListView)findViewById(R.id.observedListView);
        ListView participant2View = (ListView) findViewById(R.id.observerListView);

        String participant1 = participant1View.getSelectedItem().toString();
        String participant2 = participant2View.getSelectedItem().toString();

        // Check if selection was complete

        // Launch Main activity
        Intent mainActivityIntent = new Intent(this, UserSelectActivity.class);
        mainActivityIntent.putExtra("sessionName", sessionName);
        mainActivityIntent.putExtra("participant1", participant1);
        mainActivityIntent.putExtra("participant2", participant2);
        startActivity(mainActivityIntent);

    }

    @Override
    public void onActionComplete(String tag, ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.OK){
            if (tag.equals("get_users")){
                try {

                    dialog.dismiss();

                    JSONArray body = new JSONArray(response.getBody());
                    JSONObject item;

                    String[] users = new String[body.length()];

                    for (int i = 0; i < body.length(); i++) {
                        item = body.getJSONObject(i);
                        users[i] = item.getString("name");
                    }

                    listAdapter = new ArrayAdapter<String>(this, R.layout.activity_user_select, users);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView observedView = (ListView)findViewById(R.id.observedListView);
                            ListView observerView = (ListView) findViewById(R.id.observerListView);

                            observedView.setAdapter(listAdapter);
                            observerView.setAdapter(listAdapter);
                        }
                    });
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
