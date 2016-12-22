package com.junyenhuang.birdhouse.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class WebRequest {
    static String response = null;
    public final static int GETRequest = 1;
    public final static int POSTRequest = 2;

    //Constructor with no parameter
    public WebRequest() {
    }
    /**
     * Making web service call
     *
     * @url – url to make web request
     * @requestMethod – http request method
     */
    public String makeWebServiceCall(String url, int requestMethod) {
        return this.makeWebServiceCall(url, requestMethod, null);
    }
    /**
     * Making web service call
     *
     * @url – url to make web request
     * @requestMethod – http request method
     * @params – http request params
     */
    public String makeWebServiceCall(String urlString, int requestMethod,
                                     HashMap<String, String> params) {
        URL url;
        String response = "";
        try {
            url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15001);
            conn.setConnectTimeout(15001);
            //conn.setDoInput(true);
            //conn.setDoOutput(true);
            //Log.d("requestMethod", " > " + requestMethod);
            if (requestMethod == POSTRequest) {
                conn.setRequestMethod("POST");
            } else if (requestMethod == GETRequest) {
                conn.setRequestMethod("GET");
            }

            //Log.d("params", " > " + params);
            if (params != null) {
                OutputStream ostream = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(ostream));//, "US-ASCII"));
                StringBuilder requestResult = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        requestResult.append("&");
                    }
                    //requestResult.append(URLEncoder.encode(entry.getKey(), "US-ASCII"));
                    //requestResult.append("=");
                    //requestResult.append(URLEncoder.encode(entry.getValue(), "US-ASCII"));
                    //requestResult.append(entry.getKey());
                    //requestResult.append("=");
                    requestResult.append(entry.getValue());
                    //Log.d("requestResult", " > " + requestResult.toString());
                }
                writer.write(requestResult.toString());
                writer.flush();
                writer.close();
                ostream.close();
            }

            int responseCode = conn.getResponseCode();
            //Log.d("responseCode", " > " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
            //Log.d("response", " > " + response);
        } catch (Exception e) {
            Log.e("EXCEPTION OCCURRED!", "");
            e.printStackTrace();
        }
        return response;
    }
}