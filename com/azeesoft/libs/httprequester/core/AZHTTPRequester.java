/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.azeesoft.libs.httprequester.core;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * @author azizt
 */
public class AZHTTPRequester {

    static final int GET = 0, POST = 1;

    private String URL, leftout = "", realName = "";
    private int method = POST;
    private boolean cancelled = false;
    protected List<NameValuePair> nameValuePairs;
    private String resultString = "";
    private JSONObject jobj;
    private List<OnPrepareListener> onPrepareListenerList = new ArrayList<>();
    private List<OnResultListener> onResultListenerList = new ArrayList<>();
    private List<OnErrorListener> onErrorListenerList = new ArrayList<>();

    private MultipartEntityBuilder multipartEntityBuilder;

    private UiUpdater uiUpdater;

    public AZHTTPRequester(String url) {
        this(url, POST);
    }

    public AZHTTPRequester(String url, int method) {
        this.URL = url;
        this.method = method;
        if (method == POST) {
            nameValuePairs = new ArrayList<>();
            multipartEntityBuilder = MultipartEntityBuilder.create();
        }
    }

    public void setUiUpdater(UiUpdater uiUpdater) {
        this.uiUpdater = uiUpdater;
    }

    public void addParam(String name, int value) {
        addParam(name, Integer.toString(value));
    }

    public void addParam(String name, String value) {
        addParam(name, value, true);
    }

    public void addParam(String name, String value, boolean leaveable) {
        if (method == POST) {
            value = value.trim();
            if (value.equals("") && !leaveable) {
                leftout = name;
                realName = name;
            }

            nameValuePairs.add(new BasicNameValuePair(name, value));
            multipartEntityBuilder.addTextBody(name, value, ContentType.TEXT_PLAIN);
        }
    }

    public void addParam(String name, String value, String actualName) {
        if (nameValuePairs != null) {
            value = value.trim();
            if (value.equals("") && leftout.equals("")) {
                leftout = actualName;
                realName = name;
            }

            nameValuePairs.add(new BasicNameValuePair(name, value));
            multipartEntityBuilder.addTextBody(name, value, ContentType.TEXT_PLAIN);
        }
    }

    public String getParam(String name) {
        if (nameValuePairs != null) {
            for (NameValuePair nameValuePair : nameValuePairs) {
                if (nameValuePair.getName().equals(name)) {
                    return nameValuePair.getValue();
                }
            }
        }

        return "";
    }

    public void addFile(String name, File file) {
        multipartEntityBuilder.addBinaryBody(name, file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
    }

    public String getResultString() {
        return resultString;
    }

    public void addOnResultListener(OnResultListener orl) {
        onResultListenerList.add(orl);
    }

    public void addOnPrepareListener(OnPrepareListener oril) {
        onPrepareListenerList.add(oril);
    }

    public void addOnErrorListener(OnErrorListener oel) {
        onErrorListenerList.add(oel);
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void sendRequest() {

        execPrepares();

        if (!leftout.equals("")) {
            execErrors(leftout + " cannot be left empty");
            return;
        }

        if (!cancelled) {
            new Thread(() -> {
                try {
                    resultString = performHTTPRequest();
                    jobj = new JSONObject(resultString);
                } catch (Exception ex) {
                    //Logger.getLogger(AZHTTPRequester.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Exception :" + ex);
                }

                //QT.PL("jobj: " + jobj);

                if (cancelled) {
                    return;
                }

                Runnable runnable;
                if (jobj == null) {
//                    System.out.println(msg);
//                    onError(msg);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            String msg = "Error Connecting to Server";
                            execErrors(msg);
                        }
                    };
                } else {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            execResults(jobj);
                        }
                    };
                }

                if (uiUpdater != null) {
                    uiUpdater.runOnUiThread(runnable);
                } else {
                    runnable.run();
                }

            }).start();
        }
    }

    private void execPrepares() {
//        onPrepare(this);
        for (OnPrepareListener onPrepareListener : onPrepareListenerList)
            onPrepareListener.onPrepare(this);
    }

    private void execResults(JSONObject jsonObject) {
//        onResult(jsonObject);
        for (OnResultListener onResultListener : onResultListenerList)
            onResultListener.onResult(this, jsonObject);
    }

    private void execErrors(String msg) {
//        onError(msg);
        for (OnErrorListener onErrorListener : onErrorListenerList)
            onErrorListener.onError(this, msg);
    }

    private String performHTTPRequest() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        StringBuffer result = new StringBuffer();

        if (method == GET) {
            HttpGet request = new HttpGet(URL);
            request.addHeader("User-Agent", USER_AGENT);
            HttpResponse response = client.execute(request);

//            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } else {
            HttpPost post = new HttpPost(URL);

            post.setHeader("User-Agent", USER_AGENT);
//            post.setEntity(new UrlEncodedFormEntity(this.nameValuePairs));
            post.setEntity(multipartEntityBuilder.build());

            HttpResponse response = client.execute(post);
//            System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        }

        return result.toString();
    }


    /*public abstract void onPrepare(AZHTTPRequester AZHTTPRequester);

    public abstract void onResult(JSONObject jobj);

    public abstract void onError(String errMsg);*/

    public interface OnResultListener {
        void onResult(AZHTTPRequester azhttpRequester, JSONObject jobj);
    }

    public interface OnPrepareListener {
        void onPrepare(AZHTTPRequester azhttpRequester);
    }

    public interface OnErrorListener {
        void onError(AZHTTPRequester azhttpRequester, String errMsg);
    }

    public interface UiUpdater {
        void runOnUiThread(Runnable runnable);
    }

}
