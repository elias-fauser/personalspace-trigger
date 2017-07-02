package com.personalspace.personalspace;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.View;
import android.widget.ExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserSelectActivity extends AppCompatActivity implements RemoteSession.RemoteSessionDelegate{

    RemoteSession session = null;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String sessionName = savedInstanceState.getString("session");

        //request remote session start
        session = RemoteSession.getInstance(sessionName);

        Thread workerThread = new Thread(session);
        workerThread.start();

        session.getAllUsers(this);

        //show the progress dialog
        dialog.setMessage("Retrieving Users. Please Wait...");
        dialog.setCancelable(false);
        dialog.show();
    }


    @Override
    public void onActionComplete(String tag, ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.ACCEPTED){
            if (tag.equals("get_users")){
                try {
                    JSONArray body = new JSONArray(response.getBody());
                    JSONObject item;

                    ExpandableListView observedView = (ExpandableListView)findViewById(R.id.observedListView);
                    ExpandableListView observerView = (ExpandableListView) findViewById(R.id.observerListView);

                    for (int i = 0; i < body.length(); i++) {
                        item = body.getJSONObject(i);

                    }

                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
