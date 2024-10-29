package com.topflytech.bleAntiLost.data;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by admin on 2016/11/12.
 */

public class OpenAPI {

    private static final String URL = "http://openapi.tftiot.com:8050/v1/";

    private static class SingletonHolder {
        static OpenAPI openAPI = new OpenAPI();
    }
    private ExecutorService openapiThreadPool = Executors.newFixedThreadPool(15);
    public static OpenAPI instance() {
        return SingletonHolder.openAPI;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    private String access_token;
    private boolean isShowGoToSignIn = false;
    public interface Callback {
        enum StatusCode {
            OK,
            LOGIN,
            TIMEOUT,
            ERROR
        }

        void callback(StatusCode code, String result);
    }


    private void action(final String urlStr, final JSONObject jsonObject, final Callback callback){
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... params) {
                try {
                    HttpURLConnection conn = getHttpURLConnection(urlStr, "POST");
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

                    writer.write(jsonObject.toString());
                    writer.close();
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer jsonString = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                    conn.disconnect();
                    String resultStr = jsonString.toString();
                    JSONObject jsonObj = new JSONObject(resultStr);
                    return jsonObj;
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (null == result) {
                    callback.callback(Callback.StatusCode.ERROR,"");
                } else {
                    callback.callback(Callback.StatusCode.OK,result.toString());
                }
            }
        }.executeOnExecutor(openapiThreadPool);
    }

    private void delete(final Callback callback,final String urlStr, final HashMap<String,String> httpParams){
        commonPost(callback,urlStr,httpParams,"DELETE");
    }

    private void put(final Callback callback,final String urlStr, final HashMap<String,String> httpParams){
        commonPost(callback,urlStr,httpParams,"PUT");
    }

    private void post(final Callback callback,final String urlStr, final HashMap<String,String> httpParams){
        commonPost(callback,urlStr,httpParams,"POST");
    }


    private void commonPost(final Callback callback, final String urlStr, final HashMap<String, String> httpParams, final String requestMethod){
        if (access_token == null || access_token.length() <= 0){
            Log.e("openApiToken","access_token null");
            callback.callback(Callback.StatusCode.LOGIN,"access_token null");
            return;
        }

        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... params) {
                StringBuffer sb = new StringBuffer();
                JSONObject object = new JSONObject();
                if (httpParams != null){
                    for (String key : httpParams.keySet()){
                        try {
                            object.put(key,httpParams.get(key));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    HttpURLConnection conn = getHttpURLConnection(urlStr +"?access-token=" + access_token, requestMethod);// + "&" + param
                    if (!requestMethod.equals("DELETE")){
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                        writer.write(object.toString());
                        writer.close();
                    } else {
                        conn.setDoOutput(false);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer jsonString = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                    conn.disconnect();
                    String resultStr = jsonString.toString();
                    JSONObject jsonObj = new JSONObject(resultStr);
                    return jsonObj;
                } catch (Exception e) {
                    e.printStackTrace();
                    JSONObject err = new JSONObject();
                    try {
                        err.put("status",-1);
                        err.put("err",e.getLocalizedMessage());
                        return err;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (null == result) {
                    callback.callback(Callback.StatusCode.ERROR,"");
                } else {
                    try {
                        int errCode = result.getInt("status");
                        if (errCode == -1){
                            callback.callback(Callback.StatusCode.ERROR,result.toString());
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.callback(Callback.StatusCode.OK,result.toString());
                }
            }
        }.executeOnExecutor(openapiThreadPool);
    }

    private void commonPostNoNeedToken(final Callback callback, final String urlStr, final HashMap<String, String> httpParams, final String requestMethod,String actionName){

        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... params) {
                StringBuffer sb = new StringBuffer();
                JSONObject object = new JSONObject();
                if (httpParams != null){
                    for (String key : httpParams.keySet()){
                        try {
                            object.put(key,httpParams.get(key));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(actionName != null){
                    JSONObject outObj = new JSONObject();
                    try {
                        outObj.put(actionName,object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    object = outObj;
                }
                try {
                    HttpURLConnection conn = getHttpURLConnection(urlStr , requestMethod);// + "&" + param
                    if (!requestMethod.equals("DELETE")){
                        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                        writer.write(object.toString());
                        writer.close();
                    } else {
                        conn.setDoOutput(false);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer jsonString = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                    conn.disconnect();
                    String resultStr = jsonString.toString();
                    JSONObject jsonObj = new JSONObject(resultStr);
                    return jsonObj;
                } catch (Exception e) {
                    e.printStackTrace();
                    JSONObject err = new JSONObject();
                    try {
                        err.put("status",-1);
                        err.put("err",e.getLocalizedMessage());
                        return err;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (null == result) {
                    callback.callback(Callback.StatusCode.ERROR,"");
                } else {
                    try {
                        int errCode = result.getInt("status");
                        if (errCode == -1){
                            callback.callback(Callback.StatusCode.ERROR,result.toString());
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.callback(Callback.StatusCode.OK,result.toString());
                }
            }
        }.executeOnExecutor(openapiThreadPool);
    }

    private void getNotNeedAccessToken(final Callback callback,final String urlStr, final HashMap<String,String> httpParams){
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... params) {
                String param = "";
                if (httpParams != null) {
                    StringBuffer sb = new StringBuffer();
                    for (String key : httpParams.keySet()) {
                        String keyValue = key + "=" + httpParams.get(key);
                        sb.append(keyValue);
                        sb.append("&");
                    }
                    param = sb.substring(0, sb.length() - 1);
                }
                try {
                    HttpURLConnection conn = getHttpURLConnection(urlStr +"?"  + param,"GET");
                    conn.setDoOutput(false);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer jsonString = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                    conn.disconnect();
                    String resultStr = jsonString.toString();
                    JSONObject jsonObj = new JSONObject(resultStr);
                    return jsonObj;
                }  catch (Exception e) {
                    e.printStackTrace();
                    JSONObject err = new JSONObject();
                    try {
                        err.put("code",-1);
                        err.put("err",e.getLocalizedMessage());
                        return err;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (null == result) {
                    callback.callback(Callback.StatusCode.ERROR,"");
                } else {
                    try {
                        int errCode = result.getInt("code");
                        if (errCode == -1){
                            callback.callback(Callback.StatusCode.ERROR,result.toString());
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.callback(Callback.StatusCode.OK,result.toString());
                }
            }
        }.executeOnExecutor(openapiThreadPool);
    }

    private void get(final Callback callback,final String urlStr, final HashMap<String,String> httpParams){
        if (access_token == null || access_token.length() <= 0){
            Log.e("openApiToken","access_token null");
            callback.callback(Callback.StatusCode.LOGIN,"access_token null");
            return;
        }
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... params) {
                String param = "";
                if (httpParams != null) {
                    StringBuffer sb = new StringBuffer();
                    for (String key : httpParams.keySet()) {
                        String keyValue = key + "=" + httpParams.get(key);
                        sb.append(keyValue);
                        sb.append("&");
                    }
                    param = sb.substring(0, sb.length() - 1);
                }
                try {
                    HttpURLConnection conn = getHttpURLConnection(urlStr +"?access-token=" + access_token + "&" + param,"GET");
                    conn.setDoOutput(false);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer jsonString = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonString.append(line);
                    }
                    br.close();
                    conn.disconnect();
                    String resultStr = jsonString.toString();
                    JSONObject jsonObj = new JSONObject(resultStr);
                    return jsonObj;
                }  catch (Exception e) {
                    e.printStackTrace();
                    JSONObject err = new JSONObject();
                    try {
                        err.put("status",-1);
                        err.put("err",e.getLocalizedMessage());
                        return err;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (null == result) {
                    callback.callback(Callback.StatusCode.ERROR,"");
                } else {
                    try {
                        int errCode = result.getInt("status");
                        if (errCode == -1){
                            callback.callback(Callback.StatusCode.ERROR,result.toString());
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.callback(Callback.StatusCode.OK,result.toString());
                }
            }
        }.executeOnExecutor(openapiThreadPool);
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection(String urlStr, String requestMethod) throws IOException {
        URL url;
        url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        return conn;
    }


    public void getServerVersion(String deviceType,final Callback callback){
        String urlStr = URL + "sensor-upgrade-control-out";
        HashMap<String,String> params = new HashMap<>();
        params.put("device_type",deviceType);
        params.put("opr_type","getSensorVersion");
        getNotNeedAccessToken(callback,urlStr,params);
//        commonPostNoNeedToken(callback,urlStr,params,"POST","getSensorVersion");
    }

    public void getBetaServerVersion(String deviceType,String mac,final Callback callback){
        String urlStr = URL + "sensor-upgrade-control-out";
        HashMap<String,String> params = new HashMap<>();
        params.put("device_type",deviceType);
        params.put("mac",mac);
        params.put("opr_type","getSensorBetaVersion");
        params.put("is_debug",MyUtils.isDebug ? "1" : "0");
        getNotNeedAccessToken(callback,urlStr,params);
//        commonPostNoNeedToken(callback,urlStr,params,"POST","getSensorBetaVersion");
    }

}
