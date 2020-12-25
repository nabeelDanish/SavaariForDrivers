package com.example.savaari_driver.services.network;

import android.text.TextUtils;
import android.util.Log;
import com.example.savaari_driver.entity.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// This class holds static functions for interacting with the API Layer
public class NetworkUtil
{
    // Main Attributes
    private static NetworkUtil networkUtil = null;
    private static final String TAG = "NetworkUtil";
    private static final String urlAddress = "https://34b5b53def1a.ngrok.io/"; // remember to add a "/" at the end of the url

    // For Wrapping and Unwrapping
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Managing Cookies
    private static final java.net.CookieManager msCookieManager = new java.net.CookieManager(null, CookiePolicy.ACCEPT_ALL);
    static final String COOKIES_HEADER = "Set-Cookie";
    private Map<String, List<String>> headerFields;
    private List<String> cookiesHeader;

    // ---------------------
    // SINGLETON METHODS
    // ---------------------
    // Private Constructor
    private NetworkUtil() {
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
    private String sendPost(String urlAddress, JSONObject jsonParam) {
        try
        {
            // Creating the HTTP Connection
            URL url = new URL(urlAddress);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");

            /*
             * Add cookies to request header
             * While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
             * */
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                // Log.d(TAG, "sendPost: Found existing Cookies!");
                conn.setRequestProperty("Cookie",
                        TextUtils.join(",",  msCookieManager.getCookieStore().getCookies()));
            }
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

            // Save cookie
            headerFields = conn.getHeaderFields();
            cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            // Sending the Response Back to the User in JSON
            Scanner scanner;
            try
            {
                scanner = new Scanner(conn.getInputStream());
                String response = null;

                if (scanner.hasNext()) {
                    response = scanner.useDelimiter("\\Z").next();
                    Log.d(TAG, "sendPost: " + response);
                }
                else {
                    Log.d(TAG, "sendPost: received null Input Stream");
                }
                scanner.close();
                conn.disconnect();
                return response;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        catch (Exception e)
        {
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

            // Sending JSON
            return sendPost(urlAddress + "saveDriverLocation", jsonParam) != null? 1 : 0;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    /*
    *   SET OF DRIVER-SIDE MATCHMAKING FUNCTIONS ----------------------------------------------------
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

            return (sendPost(urlAddress + "add_driver", jsonParam) != null);
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
            String obj = sendPost(urlAddress + "login_driver", jsonParam);
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
    // Persist Connection
    public boolean persistConnection(int userID)
    {
        try {
            // Creating dummy parameter
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("USER_ID", userID);

            // Sending Request
            String result = sendPost(urlAddress + "persistDriverLogin", jsonObject);
            if (result != null) {
                JSONObject obj = new JSONObject(result);
                return obj.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Logout Call
    public boolean logout(int userID)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("USER_ID", userID);
            String result = sendPost(urlAddress + "logout_driver", jsonObject);
            if (result != null) {
                return new JSONObject(result).getInt("STATUS_CODE") == 200;
            } else {
                Log.d(TAG, "logout: returned NULL!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Loading User Data
    public Driver loadUserData(int currentUserID)
    {
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("USER_ID", currentUserID);
            String result = sendPost(urlAddress + "driver_data", jsonParam);
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
            String resultString = sendPost(urlAddress + "getDriverLocations", jsonParam);
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
            return sendPost(urlAddress + "setMarkActive", jsonParam) != null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "setMarkActive(): Exception thrown!");
            return false;
        }
    }
    // Check Ride Request Status
    public RideRequest startMatchmaking(int userID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("USER_ID", userID);
            String result = sendPost(urlAddress + "startMatchmaking", jsonObject);
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

    // Check Ride Request Status
    public RideRequest checkRideRequestStatus(int userID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("USER_ID", userID);
            String result = sendPost(urlAddress + "checkRideRequestStatus", jsonObject);
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
            String result = sendPost(urlAddress + "checkRideStatus", jsonParam);
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

            String result = sendPost(urlAddress + "confirmRideRequest", jsonParam);
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
            String result = sendPost(urlAddress + "markArrival", jsonObject);
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
            String result = sendPost(urlAddress + "startRideDriver", jsonObject);
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
            String result =  sendPost(urlAddress + "markArrivalAtDestination", jsonObject);
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
    public boolean endRideWithPayment(int rideID, Payment payment, int driverID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", rideID);
            jsonObject.put("AMNT_PAID", payment.getAmountPaid());
            jsonObject.put("CHANGE", payment.getChange());
            jsonObject.put("PAYMENT_MODE", payment.getPaymentMode());
            jsonObject.put("DRIVER_ID", driverID);
            Log.d(TAG, "endRideWithPayment: " + jsonObject.toString());
            String result = sendPost(urlAddress + "endRideWithPayment", jsonObject);
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
    // Sending Registration Request
    public boolean sendRegistrationRequest(Driver driver)
    {
        JSONObject jsonObject = new JSONObject();
        // Debugging Part
        Driver testDriver = new Driver();
        try {
            String obj = objectMapper.writeValueAsString(testDriver);
            try {
                JSONObject tempJSON = new JSONObject(obj);
                String result = sendPost(urlAddress + "jacksonTest", tempJSON);
                if (result != null) {
                    Log.d(TAG, "sendRegistrationRequest: Jackson Test: found something");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // Debugging Part
        try {
            jsonObject.put("USER_ID", driver.getUserID());
            jsonObject.put("FIRST_NAME", driver.getFirstName());
            jsonObject.put("LAST_NAME", driver.getLastName());
            jsonObject.put("PHONE_NO", driver.getPhoneNo());
            jsonObject.put("CNIC", driver.getCNIC());
            jsonObject.put("LICENSE_NUMBER", driver.getLicenseNumber());
            Log.d(TAG, "sendRegistrationRequest: jsonObject = " + jsonObject.toString());
            String result = sendPost(urlAddress + "/registerDriver", jsonObject);
            if (result != null) {
                jsonObject = new JSONObject(result);
                return jsonObject.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "sendRegistrationRequest: Exception thrown!");
            return false;
        }
    }
    public boolean sendVehicleRegistrationRequest(Driver driver, Vehicle vehicle)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("DRIVER_ID", driver.getUserID());
            jsonObject.put("MAKE", vehicle.getMake());
            jsonObject.put("MODEL", vehicle.getModel());
            jsonObject.put("YEAR", vehicle.getYear());
            jsonObject.put("NUMBER_PLATE", vehicle.getNumberPlate());
            jsonObject.put("COLOR", vehicle.getColor());
            jsonObject.put("STATUS", vehicle.getStatus());

            String result = sendPost(urlAddress + "sendVehicleRequest", jsonObject);
            if (result != null) {
                jsonObject = new JSONObject(result);
                return jsonObject.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Selecting Vehicle
    public boolean selectActiveVehicle(int driverID, int vehicleID)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("USER_ID", driverID);
            jsonObject.put("ACTIVE_VEHICLE_ID", vehicleID);
            String result = sendPost(urlAddress + "selectActiveVehicle", jsonObject);
            if (result != null) {
                jsonObject = new JSONObject(result);
                return jsonObject.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Give Feedback for Rider
    public boolean giveFeedbackForRider(Ride ride, float rating)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RIDE_ID", ride.getRideID());
            jsonObject.put("RIDER_ID", ride.getRider().getUserID());
            jsonObject.put("RATING", rating);
            String result = sendPost(urlAddress + "giveFeedbackForRider", jsonObject);
            if (result != null) {
                jsonObject = new JSONObject(result);
                return jsonObject.getInt("STATUS") == 200;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /*  END OF CLASS */
} // End of Class
