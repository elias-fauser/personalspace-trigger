package com.personalspace.triggerapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class MainActivity extends AppCompatActivity implements RemoteSession.RemoteSessionDelegate{

    private SeekBar distance = null;

    RemoteSession session = null;
    String sessionName = null;
    String participant1, participant2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        distance = (SeekBar)findViewById(R.id.seekBar5);

        //request remote session start
        Intent intent = getIntent();
        sessionName = intent.getStringExtra("sessionName");
        session = RemoteSession.getInstance(sessionName);

        participant1 = intent.getStringExtra("participant1");
        participant2 = intent.getStringExtra("participant2");

        Thread workerThread = new Thread(session);
        workerThread.start();

/*        @Override
        protected void onStart(){
            super.onStart();
            new HttpRequestTask().execute();
        }
*/
        distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double distance = (double) seekBar.getProgress();

                try {
                    JSONObject body = new JSONObject();
                    JSONObject distanceData = new JSONObject();
                    distanceData.put("distance", distance);
                    body.put("data", distanceData);

                    session.sendMessage(participant1, body, MainActivity.this);
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

        setSupportActionBar(toolbar);

    }

    @Override
    public void onActionComplete(String tag, ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.OK && tag.equals("send_message")){

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
/*
private class HttpRequestTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(Void... params) {
        try {
            final String url = "https://personalspace.herokuapp.com/";
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            int distance = restTemplate.getForObject(url, distance);
            return Integer.toString(distance);
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }
/*
    @Override
    protected void onPostExecute(Greeting greeting) {
        TextView greetingIdText = (TextView) findViewById(R.id.id_value);
        TextView greetingContentText = (TextView) findViewById(R.id.content_value);
        greetingIdText.setText(greeting.getId());
        greetingContentText.setText(greeting.getContent());
    }
*/
/**/