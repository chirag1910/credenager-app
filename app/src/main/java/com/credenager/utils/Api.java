package com.credenager.utils;

import com.credenager.interfaces.ApiResponse;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Api {
    static String apiUrl = "https://credenager.herokuapp.com/";

    public static void deleteAccount(String jwtToken, String password, String key, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("password", password);
            body.put("key", key);

            new Thread(() -> response.Response(callApi("user/delete", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void deleteGroup(String jwtToken, String id, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("_id", id);

            new Thread(() -> response.Response(callApi("group/delete", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void deleteCred(String jwtToken, String id, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("_id", id);

            new Thread(() -> response.Response(callApi("credential/delete", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void updateCred(String jwtToken, String id, String identifier, String value, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("_id", id);
            body.put("identifier", identifier);
            body.put("value", value);

            new Thread(() -> response.Response(callApi("credential/update", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void addCred(String jwtToken, String groupId, String identifier, String value, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("groupId", groupId);
            body.put("identifier", identifier);
            body.put("value", value);

            new Thread(() -> response.Response(callApi("credential/create", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void updateGroup(String jwtToken, String id, String name, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("_id", id);
            body.put("name", name);

            new Thread(() -> response.Response(callApi("group/update", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void addGroup(String jwtToken, String name, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("name", name);

            new Thread(() -> response.Response(callApi("group/create", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void getUser(String jwtToken, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            new Thread(() -> response.Response(callApi("user/", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void getUserBasic(String jwtToken, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            new Thread(() -> response.Response(callApi("user/basic", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void resetKey(String jwtToken, String password, String key, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("password", password);
            body.put("key", key);
            new Thread(() -> response.Response(callApi("user/reset/key", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void changePassword(String token, String oldPass, String newPass, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", token);
            body.put("oldPassword", oldPass);
            body.put("password", newPass);
            new Thread(() -> response.Response(callApi("user/update/password", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void resetPass(String email, int otp, String password, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("otp", otp);
            body.put("password", password);
            new Thread(() -> response.Response(callApi("user/reset/password", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }
    static public void resetPassInit(String email, ApiResponse response){
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            new Thread(() -> response.Response(callApi("user/reset/password/init", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void verifyKey(String jwtToken, String key, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("JWT_TOKEN", jwtToken);
            body.put("key", key);
            new Thread(() -> response.Response(callApi("user/verify/key", body))).start();
        }
        catch (Exception e) {
            response.Response(null);
        }
    }

    static public void signupGoogle(String key, String idToken, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("googleToken", idToken);
            body.put("key", key);
            new Thread(() -> response.Response(callApi("user/signup/google", body))).start();
        }
        catch (Exception e){
            response.Response(null);
        }
    }

    static public void loginGoogle(String idToken, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("googleToken", idToken);
            new Thread(() -> response.Response(callApi("user/login/google", body))).start();
        }
        catch (Exception e){
            response.Response(null);
        }
    }

    static public void signup(String email, String password, String key, ApiResponse response) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            body.put("key", key);
            new Thread(() -> response.Response(callApi("user/signup", body))).start();
        }
        catch (Exception e){
            response.Response(null);
        }
    }

    static public void login(String email, String password, ApiResponse response) {
        try{
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);
            new Thread(() -> response.Response(callApi("user/login", body))).start();
        }
            catch (Exception e) {
            response.Response(null);
        }
    }


    static private JSONObject callApi (String endpoint, JSONObject body) {
        String query = body.toString();

        HttpURLConnection urlConn;
        try {

            URL mUrl = new URL(apiUrl + endpoint);
            urlConn = (HttpURLConnection) mUrl.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.addRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("Content-Length", Integer.toString(query.length()));
            urlConn.setDoOutput(true);
            urlConn.getOutputStream().write(query.getBytes(StandardCharsets.UTF_8));

            InputStream messageStream = urlConn.getInputStream();
            StringBuilder message = new StringBuilder();
            int i;
            while((i=messageStream.read())!=-1){
                message.append((char) i);
            }
            urlConn.disconnect();
            return new JSONObject(message.toString());
        }
        catch (Exception e){
            try {
                JSONObject defaultResponse = new JSONObject();
                defaultResponse.put("status", "error");
                defaultResponse.put("code", 502);
                defaultResponse.put("error", Globals.APP_OFFLINE_MODE ? "No Internet Connection" : "Some error occurred, please try again later");
                return defaultResponse;
            }
            catch (Exception e1) {
                return null;
            }
        }
    }
}
