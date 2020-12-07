package com.example.savaari_driver.services.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

// This class holds static functions for interacting with the API Layer
public class NetworkUtil
{
    // Main Attributes
    private static final NetworkUtil networkUtil = new NetworkUtil();
    private static final String TAG = "NetworkUtil";
    private static final String urlAddress = "https://4b15bd13dffb.ngrok.io/"; // remember to add a "/" at the end of the url

    // Private Constructor
    private NetworkUtil()
    {
        // Empty
    }

    // -------------------------------------------------------------------------------
    //                                 Main Methods
    // -------------------------------------------------------------------------------

    public static JSONArray sendPostArray(String urlAddress, JSONObject jsonParam, boolean needResponse) throws JSONException {

        JSONArray result = new JSONArray();
        try
        {
            // Creating the HTTP Connection
            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Sending the Data and Receiving Output
            Log.i(TAG, "sendPostArray: " + jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());

            // Flushing output streams
            os.flush();
            os.close();

            Log.i(TAG, "sendPostArray: Status: " + conn.getResponseCode());
            Log.i(TAG, "sendPostArray: Response Message: " + conn.getResponseMessage());

            // Sending the Response Back to the User in JSON
            if (needResponse)
            {
                Scanner scanner;
                try
                {
                    scanner = new Scanner(conn.getInputStream());
                    String response = scanner.useDelimiter("\\Z").next();
                    JSONArray results = new JSONArray(response);
                    Log.d(TAG, "sendPostArray: " + response);
                    scanner.close();
                    conn.disconnect();
                    return results;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            result.put(0, true);
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result.put(0, false);
            return result;
        }
    }
    // Sending POST Requests
    public static JSONObject sendPost(String urlAddress, JSONObject jsonParam, boolean needResponse) throws JSONException {

        Log.d(TAG, "sendPost: urlAddress = " + urlAddress);
        JSONObject result = new JSONObject();
        try
        {
            // Creating the HTTP Connection
            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Sending the Data and Receiving Output
            Log.i(TAG, "sendPost: " + jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());

            // Flushing output streams
            os.flush();
            os.close();

            Log.i(TAG, "sendPost: Status: " + conn.getResponseCode());
            Log.i(TAG, "sendPost: Response Message: " + conn.getResponseMessage());

            // Sending the Response Back to the User in JSON
            if (needResponse)
            {
                Scanner scanner;
                try
                {
                    scanner = new Scanner(conn.getInputStream());
                    String response = scanner.useDelimiter("\\Z").next();
                    JSONObject results = new JSONObject(response);
                    Log.d(TAG, "sendPost: " + response);
                    scanner.close();
                    conn.disconnect();
                    return results;
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            result.put("result", true);
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            result.put("result", false);
            return result;
        }
    }

    // Send Last Location
    public static int sendLastLocation(int currentUserID, double latitude, double longitude)
    {
        try
        {
            // TimeStamp
            long tsLong = System.currentTimeMillis() / 1000;
            String currentTimeStamp = Long.toString(tsLong);

            // JSON
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("USER_ID", currentUserID);
            jsonParam.put("LATITUDE", latitude);
            jsonParam.put("LONGITUDE", longitude);
            jsonParam.put("TIMESTAMP", currentTimeStamp);

            // Logging
            Log.d(TAG, "sendLastLocation: User_ID: " + currentUserID);
            Log.d(TAG, "sendLastLocation: Latitude: " + latitude);
            Log.d(TAG, "sendLastLocation: Longitude: " + longitude);
            Log.d(TAG, "sendLastLocation: TimeStamp: " + currentTimeStamp);

            // Sending JSON
            return NetworkUtil.sendPost(urlAddress + "saveDriverLocation", jsonParam, false).getBoolean("result") ? 1 : 0;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    /*
    *   SET OF RIDER-SIDE MATCHMAKING FUNCTIONS ----------------------------------------------------
    */
    public static boolean findDriver(int currentUserID, double latitude, double longitude) {
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("USER_ID", currentUserID);
            jsonParam.put("LATITUDE", latitude);
            jsonParam.put("LONGITUDE", longitude);

            return (NetworkUtil.sendPost(urlAddress, jsonParam, true).getInt("STATUS_CODE") == 200);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d("NetworkUtil: ", "findDriver() Exception");
            return false;
        }
    }

    // Sign-Up
    public static boolean signup(String username, String emailAddress, String password)
    {
        try {
            Log.d("NetworkUtil: ", "signup() called");
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("email_address", emailAddress);
            jsonParam.put("password", password);

            return (sendPost(urlAddress + "add_driver", jsonParam, true).getInt("STATUS_CODE") == 200);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "signup(): Failed!");
            return false;
        }
    }
    // Login
    public static int login(String username, String password)
    {
        try
        {
            // Creating the JSON Object
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("password", password);

            // Sending Request
            JSONObject results = sendPost(urlAddress + "login_driver", jsonParam, true);

            return results.getInt("USER_ID");
        } catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
    // Loading User Data
    public static JSONObject loadUserData(int currentUserID)
    {
        JSONObject jsonParam = new JSONObject();
        try
        {
            jsonParam.put("USER_ID", currentUserID);
            return sendPost(urlAddress + "driver_data", jsonParam, true);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Get User Locations
    public static JSONArray getUserLocations()
    {
        JSONObject jsonParam = new JSONObject();
        try
        {
            jsonParam.put("Dummy", 0);
            return sendPostArray(urlAddress + "getDriverLocations", jsonParam, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    // Set Mark Active
    public static JSONObject setMarkActive(int userID, int active_status)
    {
        JSONObject jsonParam = new JSONObject();
        try
        {
            jsonParam.put("USER_ID", userID);
            jsonParam.put("ACTIVE_STATUS", active_status == 1);
            return sendPost(urlAddress + "setMarkActive", jsonParam, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "setMarkActive(): Exception thrown!");
            return null;
        }
    }
    // Check Ride Status
    public static JSONObject checkRideStatus(int userID)
    {
        JSONObject jsonParam = new JSONObject();
        try
        {
            jsonParam.put("USER_ID", userID);
            return sendPost(urlAddress + "checkRideStatus", jsonParam, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "checkRideStatus(): Exception thrown!");
            return null;
        }
    }
    // Confirming Ride Request
    public static JSONObject confirmRideRequest(int userID, int found_status, int riderID)
    {
        JSONObject jsonParam = new JSONObject();
        try
        {
            jsonParam.put("USER_ID", userID);
            jsonParam.put("FOUND_STATUS", found_status);
            jsonParam.put("RIDER_ID", riderID);
            Log.d(TAG, "confirmRideRequest(): " + jsonParam.toString());
            return sendPost(urlAddress + "confirmRideRequest", jsonParam, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "confirmRideRequest(): Exception thrown!");
            return null;
        }
    }
    // Marking Arrival
    public static JSONObject markArrival(int rideID) {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("RIDE_ID", rideID);
            Log.d(TAG, "markArrival(): " + jsonObject.toString());
            return sendPost(urlAddress + "markArrival", jsonObject, true);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "markArrival(): Exception thrown!");
            return null;
        }
    }

    // Starting Ride
    public static JSONObject startRide(int rideID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            Log.d(TAG, "startRide(): " + jsonObject.toString());
            return sendPost(urlAddress + "startRideDriver", jsonObject, true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startRide(): Exception thrown!");
            return null;
        }
    }

    // Ending Ride
    public static JSONObject markDriverAtDestination(int rideID, double dist_travelled, int driverID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            jsonObject.put("DIST_TRAVELLED", dist_travelled);
            jsonObject.put("DRIVER_ID", driverID);
            Log.d(TAG, "endRide(): " + jsonObject.toString());
            return sendPost(urlAddress + "markArrivalAtDestination", jsonObject, true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "endRide(): Exception thrown!");
            return null;
        }
    }

    // Ending Ride with Payment
    public static JSONObject endRideWithPayment(int rideID, double amountPaid, int driverID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            jsonObject.put("AMNT_PAID", amountPaid);
            jsonObject.put("DRIVER_ID", driverID);
            Log.d(TAG, "endRideWithPayment: " + jsonObject.toString());
            return sendPost(urlAddress + "endRideWithPayment", jsonObject, true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "endRideWithPayment: Exception thrown!");
            return null;
        }
    }
}
