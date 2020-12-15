package com.example.savaari_driver.services.network;

import android.util.Log;
import com.example.savaari_driver.entity.Driver;
import com.example.savaari_driver.entity.Location;
import com.example.savaari_driver.entity.Ride;
import com.example.savaari_driver.entity.RideRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

// This class holds static functions for interacting with the API Layer
public class NetworkUtil
{
    // Main Attributes
    private static NetworkUtil networkUtil = null;
    private static final String TAG = "NetworkUtil";
    private static final String urlAddress = "https://5fd94018bf4b.ngrok.io/"; // remember to add a "/" at the end of the url
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Private Constructor
    private NetworkUtil()
    {
        // Empty
    }
    public static NetworkUtil getInstance() {
        if (networkUtil == null) {
            networkUtil = new NetworkUtil();
        }
        return networkUtil;
    }
    // -------------------------------------------------------------------------------
    //                                 Main Methods
    // -------------------------------------------------------------------------------
    // Sending POST Requests
    public String sendPost(String urlAddress, JSONObject jsonParam, boolean needResponse)
    {
        try {
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
            if (needResponse) {
                Scanner scanner;
                try {
                    scanner = new Scanner(conn.getInputStream());
                    String results = null;
                    if (scanner.hasNext()) {
                        String response = scanner.useDelimiter("\\Z").next();

                        if (response != null) {
                            results = response;
                        }
                        Log.d(TAG, "sendPost: " + response);
                    }
                    else {
                        Log.d(TAG, "sendPost: received null Input Stream");
                        results = null;
                    }
                    scanner.close();
                    conn.disconnect();
                    return results;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------------------------------------------------------
    //                                 NETWORK OPERATIONS
    // -------------------------------------------------------------------------------

    // Send Last Location
    public int sendLastLocation(int currentUserID, double latitude, double longitude)
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
            return sendPost(urlAddress + "saveDriverLocation", jsonParam, false) != null? 1 : 0;
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
    // Sign-Up
    public boolean signup(String username, String emailAddress, String password)
    {
        try {
            Log.d("NetworkUtil: ", "signup() called");
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("email_address", emailAddress);
            jsonParam.put("password", password);

            return (sendPost(urlAddress + "add_driver", jsonParam, true) != null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "signup(): Failed!");
            return false;
        }
    }
    // Login
    public int login(String username, String password)
    {
        try {
            // Creating the JSON Object
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("username", username);
            jsonParam.put("password", password);

            // Sending Request
            String obj = sendPost(urlAddress + "login_driver", jsonParam, true);
            if (obj != null) {
                JSONObject results = new JSONObject(obj);
                return results.getInt("USER_ID");
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    // Loading User Data
    public Driver loadUserData(int currentUserID)
    {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("USER_ID", currentUserID);
            String result = sendPost(urlAddress + "driver_data", jsonParam, true);
            if (result != null) {
                return objectMapper.readValue(result, Driver.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get User Locations
    public ArrayList<Location> getUserLocations()
    {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("Dummy", 0);
            String resultString = sendPost(urlAddress + "getDriverLocations", jsonParam, true);
            if (resultString == null) {
                return null;
            } else {
                return objectMapper.readValue(resultString,
                        objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Location.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Set Mark Active
    public boolean setMarkActive(int userID, int active_status)
    {
        JSONObject jsonParam = new JSONObject();
        try
        {
            jsonParam.put("USER_ID", userID);
            jsonParam.put("ACTIVE_STATUS", active_status == 1);
            return sendPost(urlAddress + "setMarkActive", jsonParam, true) != null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "setMarkActive(): Exception thrown!");
            return false;
        }
    }
    // Check Ride Request Status
    public RideRequest checkRideRequestStatus(int userID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("USER_ID", userID);
            String result = sendPost(urlAddress + "checkRideRequestStatus", jsonObject, true);
            if (result != null) {
                return objectMapper.readValue(result, RideRequest.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Check Ride Status
    public Ride checkRideStatus(int userID, int riderID)
    {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("USER_ID", userID);
            jsonParam.put("RIDER_ID", riderID);
            String result = sendPost(urlAddress + "checkRideStatus", jsonParam, true);
            if (result != null) {
                return objectMapper.readValue(result, Ride.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "checkRideStatus(): Exception thrown!");
            return null;
        }
    }
    // Confirming Ride Request
    public boolean confirmRideRequest(int userID, int found_status, int riderID)
    {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("USER_ID", userID);
            jsonParam.put("FOUND_STATUS", found_status);
            jsonParam.put("RIDER_ID", riderID);
            Log.d(TAG, "confirmRideRequest(): " + jsonParam.toString());

            String result = sendPost(urlAddress + "confirmRideRequest", jsonParam, true);
            if (result != null) {
                JSONObject returnObj = new JSONObject(result);
                return returnObj.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "confirmRideRequest(): Exception thrown!");
            return false;
        }
    }

    // Marking Arrival
    public boolean markArrival(int rideID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            Log.d(TAG, "markArrival(): " + jsonObject.toString());
            String result = sendPost(urlAddress + "markArrival", jsonObject, true);
            if (result != null) {
                JSONObject returnObj = new JSONObject(result);
                return returnObj.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "markArrival(): Exception thrown!");
            return false;
        }
    }

    // Starting Ride
    public boolean startRide(int rideID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            Log.d(TAG, "startRide(): " + jsonObject.toString());
            String result = sendPost(urlAddress + "startRideDriver", jsonObject, true);
            if (result != null) {
                JSONObject returnObj = new JSONObject(result);
                return returnObj.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "startRide(): Exception thrown!");
            return false;
        }
    }

    // Ending Ride
    public double markDriverAtDestination(int rideID, double dist_travelled, int driverID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            jsonObject.put("DIST_TRAVELLED", dist_travelled);
            jsonObject.put("DRIVER_ID", driverID);
            Log.d(TAG, "endRide(): " + jsonObject.toString());
            String result =  sendPost(urlAddress + "markArrivalAtDestination", jsonObject, true);
            if (result != null) {
                jsonObject = new JSONObject(result);
                return jsonObject.getDouble("FARE");
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "endRide(): Exception thrown!");
            return -1;
        }
    }

    // Ending Ride with Payment
    public boolean endRideWithPayment(int rideID, double amountPaid, int driverID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            jsonObject.put("AMNT_PAID", amountPaid);
            jsonObject.put("DRIVER_ID", driverID);
            Log.d(TAG, "endRideWithPayment: " + jsonObject.toString());
            String result = sendPost(urlAddress + "endRideWithPayment", jsonObject, true);
            if (result != null) {
                jsonObject = new JSONObject(result);
                return jsonObject.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "endRideWithPayment: Exception thrown!");
            return false;
        }
    }

    /*  END OF CLASS */
} // End of Class
