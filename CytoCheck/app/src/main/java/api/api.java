package api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.json.JSONObject;

import java.io.OutputStream;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;



//API CALLS:
//USERS:


public class api {
    //INSTANTIATE GLOBAL API OBJECT
    private static api instance = null;
    public static api getInstance() {
        if (instance == null) {

            instance = new api();
        }
        return instance;
    }

    //FOR BYPASSING HOSTNAME VERIFICATOIN! REMOVE THIS WHEN DEPLOYING LIVE. YOU WILL NEED TO REGENERATE cert.pem WITH THE FINAL AWS HOSTNAME.
    //ALSO REMEMBER TO DO THIS SERVER SIDE AS WELL
    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // Bypass hostname verification for "10.0.2.2"
            return hostname.equals("ec2-54-193-162-215.us-west-1.compute.amazonaws.com");
        }
    };


    public void sendGetRequestWithHandler(String httpsAddress, HandlerResponse handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL obj = new URL(httpsAddress);
                    HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
                    connection.setRequestMethod("GET");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    int responseCode = connection.getResponseCode();
                    System.out.println("GET Response Code :: " + responseCode);
                    if (responseCode == HttpsURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // print result
                        handler.handleResponse(response.toString());
                    } else {
                        Log.d("failure", "Get request did not work :(");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exceptions
                }
            }
        });
        thread.start();
    }

    public void sendGetRequestWithHandlerWithToken(String httpsAddress, String token, HandlerResponse handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL obj = new URL(httpsAddress);
                    HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Authorization", token);
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    int responseCode = connection.getResponseCode();
                    System.out.println("GET Response Code :: " + responseCode);
                    if (responseCode == HttpsURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // print result
                        handler.handleResponse(response.toString());
                    } else {
                        Log.d("failure", "Get request did not work :(");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exceptions
                }
            }
        });
        thread.start();
    }


    public void sendPostRequest(String httpsAddress, JSONObject jsonInput) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(httpsAddress); // Replace with your URL
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    connection.setDoOutput(true);

                    try(OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    // Handle the response
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        Log.d("success", jsonInput.toString());
                    } else {
                        Log.d("fail", jsonInput.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exceptions
                }
            }
        });
        thread.start();
    }

    public void sendPostRequestWithHandler(String httpsAddress, JSONObject jsonInput, HandlerResponse handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(httpsAddress); // Replace with your URL
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    connection.setDoOutput(true);

                    try(OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    String responseMessage = ""; // Initialize response message

                    // Read the response message if needed
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        responseMessage = response.toString();

                    }
                    Log.d("post response", responseMessage);
                    // Invoke the handler
                    if (handler != null) {
                        handler.handleResponse(responseMessage);
                    }

                } catch (Exception e) {
                    //e.printStackTrace();
                    Log.d("post response", e.toString());
                    if (handler != null) {
                        handler.handleResponse("failure");
                    }
                }
            }
        });
        thread.start();
    }



    public void sendPostRequestWithHandlerWithToken(String httpsAddress, JSONObject jsonInput, String token, HandlerResponse handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(httpsAddress); // Replace with your URL
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Authorization", token);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    connection.setDoOutput(true);

                    try(OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    String responseMessage = ""; // Initialize response message

                    // Read the response message if needed
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        responseMessage = response.toString();
                    }

                    // Invoke the handler
                    if (handler != null) {
                        handler.handleResponse(responseMessage);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });
        thread.start();
    }



    public void sendPostRequestHandler(String httpsAddress, HandlerResponse handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL obj = new URL(httpsAddress);
                    HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
                    connection.setRequestMethod("GET");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    int responseCode = connection.getResponseCode();
                    System.out.println("GET Response Code :: " + responseCode);
                    if (responseCode == HttpsURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // print result
                        handler.handleResponse(response.toString());
                    } else {
                        Log.d("failure", "Get request did not work :(");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exceptions
                }
            }
        });
        thread.start();
    }


    public void sendDeleteRequest(String httpsAddress,JSONObject jsonInput) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(httpsAddress); // Replace with your URL
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    connection.setDoOutput(true);

                    try(OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    // Handle the response
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        Log.d("success", jsonInput.toString());
                    } else {
                        Log.d("fail", jsonInput.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    public void sendDeleteRequestWithTokenWithHandler(String httpsAddress, String token, HandlerResponse handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL obj = new URL(httpsAddress);
                    HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
                    connection.setRequestMethod("DELETE");
                    connection.setRequestProperty("Authorization", token);
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    int responseCode = connection.getResponseCode();
                    System.out.println("DELETE Response Code :: " + responseCode);
                    if (responseCode == HttpsURLConnection.HTTP_OK) { // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // print result
                        handler.handleResponse(response.toString());
                    } else {
                        Log.d("failure", "DELETE request did not work :(");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exceptions
                }
            }
        });
        thread.start();
    }


    public void sendPutRequest(String httpsAddress, JSONObject jsonInput) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(httpsAddress); // Replace with your URL
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    //REMOVE THIS LATER WHEN DEPLOYED, USED HERE FOR TESTING PURPOSES
                    connection.setHostnameVerifier(hostnameVerifier);
                    connection.setDoOutput(true);

                    try(OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInput.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    // Handle the response
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        Log.d("success", jsonInput.toString());
                    } else {
                        Log.d("fail", jsonInput.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


}