package com.personalspace.personalspace;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by aanal on 7/2/17.
 */

public class RemoteSession implements Runnable {

    private static final String SERVICE_URL = "https://personalspace.herokuapp.com/";
    private static final String SESSION = "sessions";
    private static final String USERS = "sessions/users";
    private static final String NOTIFY = "sessions/users/{user}/notify";
    private static final String PASSKEY = "test1234";

    static RemoteSession instance;


    public interface RemoteSessionDelegate {
        void onActionComplete(String tag, ResponseEntity<String> response);
    }

    public enum State {
        ACTIVE("ACTIVE"), INACTIVE("INACTIVE");

        private String value;

        private State(String state) {
            this.value = state;
        }

        public String getValue() {
            return value;
        }
    }

    private class ActionRequest {
        HttpMethod method;
        String url;
        HttpEntity<String> requestEntity;
        Object[] urlParams = new Object[0];
        String tag;
        RemoteSessionDelegate delegate;
    }

    /**
     * The name of the session
     */
    private String name;

    /**
     * The id of the session
     */
    private long id;

    private RestTemplate template;

    private BlockingQueue<ActionRequest> requests;

    private RemoteSession(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Session Name cannot be null or empty");
        }
        this.name = name;
        template = new RestTemplate();
        template.getMessageConverters().add(new StringHttpMessageConverter());
        template.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
            }
        });
        requests = new LinkedBlockingQueue<>();
    }

    public static RemoteSession getInstance(String name) {
        if (instance == null) {
            instance = new RemoteSession(name);
        }
        return instance;
    }


    public void startSession(RemoteSessionDelegate delegate) {
        //prepare the request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("status", State.ACTIVE.getValue());
            requestBody.put("name", name);
            requestBody.put("passkey", PASSKEY);

            ActionRequest request = new ActionRequest();
            request.url = SERVICE_URL + SESSION;
            request.method = HttpMethod.POST;
            request.delegate = delegate;
            request.tag = "session_start";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            request.requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            requests.put(request);
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopSession(RemoteSessionDelegate delegate) {
        //prepare the request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("status", State.INACTIVE.getValue());
            requestBody.put("name", name);
            requestBody.put("passkey", PASSKEY);

            ActionRequest request = new ActionRequest();
            request.url = SERVICE_URL + SESSION;
            request.method = HttpMethod.POST;
            request.delegate = delegate;
            request.tag = "session_start";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            request.requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            requests.put(request);
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getAllUsers(RemoteSessionDelegate delegate) {
        try {
            ActionRequest request = new ActionRequest();
            request.url = SERVICE_URL + USERS;
            request.method = HttpMethod.GET;
            request.delegate = delegate;
            request.tag = "get_users";
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            request.requestEntity = new HttpEntity<>(headers);

            requests.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String username, JSONObject message, RemoteSessionDelegate delegate) {
        //prepare the request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("message", message);
            requestBody.put("passkey", PASSKEY);

            ActionRequest request = new ActionRequest();
            request.url = SERVICE_URL + NOTIFY;
            request.method = HttpMethod.POST;
            request.delegate = delegate;
            request.tag = "send_message";
            request.urlParams = new Object[]{username};
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            request.requestEntity = new HttpEntity<>(requestBody.toString(), headers);

            requests.put(request);
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                ActionRequest request = requests.take();
                ResponseEntity<String> response = template.exchange(request.url, request.method, request.requestEntity, String.class, request.urlParams);
                if (request.delegate != null) {
                    request.delegate.onActionComplete(request.tag, response);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
