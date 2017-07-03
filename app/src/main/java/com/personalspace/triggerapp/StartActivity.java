package com.personalspace.triggerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class StartActivity extends AppCompatActivity implements RemoteSession.RemoteSessionDelegate{

    private static final String TAG = StartActivity.class.getName();

    private EditText sessionName;
    private RemoteSession session;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        sessionName = (EditText) findViewById(R.id.edit_session_name);
        dialog = new ProgressDialog(this);
    }

    public void onStartSession(View view){
        if(sessionName.getText().length() == 0){
            sessionName.setError("Session Name cannot be empty");
        } else {
            String name = sessionName.getText().toString();

            //request remote session start
            session = RemoteSession.getInstance(name);

            Thread workerThread = new Thread(session);
            workerThread.start();

            session.startSession(this);

            //show the progress dialog
            dialog.setMessage("Please Wait...");
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    @Override
    public void onActionComplete(String tag, final ResponseEntity<String> response) {
        if(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED){
            //success
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Log.d(TAG, "run: Session Started");
                }
            });

            Intent selectUserIntent = new Intent(this, UserSelectActivity.class);
            selectUserIntent.putExtra("sessionName", (String) sessionName.getText().toString());
            selectUserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(selectUserIntent);
            finish();

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Log.e(TAG, "run: " + response.getBody());
                    Toast.makeText(StartActivity.this, "Could not start the session!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
