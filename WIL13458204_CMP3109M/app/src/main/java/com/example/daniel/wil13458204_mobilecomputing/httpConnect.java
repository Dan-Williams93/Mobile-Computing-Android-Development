package com.example.daniel.wil13458204_mobilecomputing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

/**
 * Created by Daniel Williams - WIL13458204 - on 12/11/2015.
 * CLASS TAKEN FROM WORKSHOP 5 CONSTRUCTED BY DEREK FOSTER
 */
public class httpConnect {

    final String TAG = "JsonParser.java";   //THIS LINE IS FOR MAKING DEBUGGING EASIER
    static String json = "";    //HOLDS THE RETURNED JSON DATA

    // CALLED FROM ACTIVITY PASSING THE URL TO THE REST API
    public String getJSONFromUrl(String url) {

        try {
            //CREATES A HTTP GET CONNECTION TO THE URL PASSED TO THE METHOD
            URL u = new URL(url);
            HttpURLConnection restConnection = (HttpURLConnection) u.openConnection();
            restConnection.setRequestMethod("GET");
            restConnection.setRequestProperty("Content-length", "0");
            restConnection.setUseCaches(false);
            restConnection.setAllowUserInteraction(false);
            restConnection.setConnectTimeout(10000);
            restConnection.setReadTimeout(10000);
            restConnection.connect();

            int status = restConnection.getResponseCode(); //RETRIEVES THE CONNECTION RESPONSE CODE

            // SWITCH STATEMENT TO CATCH HTTP ERRORS
            switch (status) {
                case 201:

                case 200:

                    //LIVE CONNECTION TO THE REST SERVICE ESTABLISHED
                    BufferedReader br = new BufferedReader(new InputStreamReader(restConnection.getInputStream()));

                    //CONSTRUCTS A STRING BUILDER USED TO STORE THE RETURNED JSON
                    StringBuilder sb = new StringBuilder();
                    String line;

                    //LOOPS THROUGH RETURNED JSON LINE BY LINE APPENDING TO THE STRINGBUILDER 'sb' VARIABLE
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    //CATCHES ERROR CONVERTING STRINGBUILDER TO STRING
                    try {
                        json = sb.toString();
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing data " + e.toString());
                    }

                    // RETURNS THE CONSTRUCTED JSON STRING
                    return json;
            }

        // HTTP 200 AND 201 ERROR HANDLING FROM THE SWITCH STATEMENT
        } catch (MalformedURLException ex) {
            Log.e(TAG, "Malformed URL ");
        } catch (IOException ex) {
            Log.e(TAG, "IO Exception ");
        }
        return null;
    }
}
