package com.bezalel.trading_api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.json.JSONObject;

public class Yandex {
    
    public String yandex_requestPaymentFromCard(
            String amount, 
            String emailfrom,
            boolean gethtmlform) throws Exception {
        
        Settings settings = new Settings();  
        Utils utils = new Utils(); 
        String url = settings.api_url_request_depositfromcard;
        String instance_id = settings.instanceid_depositfromcard;
        String commission = null;
        String result = null;        
     
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);        
        conn.setRequestMethod("POST");
        
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String orderlabel = utils.utility_getRandomString();
        System.out.println("Yandex_requestPaymentFromCard> yandex_orderlabel:" + orderlabel);
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pattern_id", "p2p"));
        params.add(new BasicNameValuePair("instance_id", instance_id));
        params.add(new BasicNameValuePair("to", "410014402693803"));
        params.add(new BasicNameValuePair("amount_due", amount)); 
        params.add(new BasicNameValuePair("label", orderlabel)); 

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();              

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode); // 200
        System.out.println("Response message: " + conn.getResponseMessage());
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_requestPaymentFromCard> response:" + result);
            JSONObject obj = new JSONObject(result);
            JSONObject objfees =  obj.getJSONObject("fees");
            commission = String.valueOf(objfees.getDouble("service"));            
        }
        
        String JSON = result;        
        JSONObject obj = new JSONObject(JSON);
        String status = obj.getString("status");
        if (status.equals("success")) {
            String request_id = obj.getString("request_id");
            System.out.println("Yandex_requestPaymentFromCard> request_id: " + request_id);
            return yandex_processPaymentFromCard(
                request_id, 
                orderlabel, 
                amount, 
                commission, 
                emailfrom, 
                gethtmlform);
        } else
            return "-1";
    }    
    
    public String yandex_processPaymentFromCard(
            String request_id, 
            String label, 
            String amount, 
            String commission, 
            String emailfrom,
            boolean gethtmlform) throws Exception {
        
        Settings settings = new Settings();       
        String url = settings.api_url_process_depositfromcard;
        String instance_id = settings.instanceid_depositfromcard;
        String result = null;
        String redirect_url= settings.yandex_redirect_url + "?label=" + label + "&amount=" + amount + "&commission=" + commission +  "&emailfrom=" + emailfrom;
        System.out.println("Yandex_processPaymentFromCard> redirect_url: " + redirect_url);
        
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);        
        conn.setRequestMethod("POST");
        
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("instance_id", instance_id)); 
        params.add(new BasicNameValuePair("request_id", request_id));
        params.add(new BasicNameValuePair("ext_auth_success_uri", redirect_url));
        params.add(new BasicNameValuePair("ext_auth_fail_uri", "http://example.com/fail"));

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();              

        int responseCode = conn.getResponseCode();
        System.out.println("Yandex_processPaymentFromCard> Response code: " + responseCode); 
        System.out.println("Yandex_processPaymentFromCard> Response message: " + conn.getResponseMessage());
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_processPaymentFromCard> response: " + result);
        }
                
        String JSON = result; 
        JSONObject obj = new JSONObject(JSON);
        String status = obj.getString("status");
        if (status.equals("ext_auth_required")) {
            String acs_uri = obj.getString("acs_uri");
            obj = obj.getJSONObject("acs_params");
            String orderId = obj.getString("orderId");
            System.out.println("Yandex_processPaymentFromCard> acs_uri: " + acs_uri + ", orderId: " + orderId);
            if (gethtmlform)
                return utility_MakeHiddenSubmitForm(orderId);
            else                
                return orderId;
        } else
            return "-1";
    }   
    
    public String yandex_requestP2PTransfer(
            String payee_email, 
            String amount, 
            String message,
            String password) throws Exception {
        
        Settings settings = new Settings();
        
        String result = null;
        String url = settings.api_url_p2p_request;
        String access_token = settings.api_url_p2p_access_token; 
        String true_password = settings.p2p_password;
        if (!true_password.equals(password))
            return "Wrong password!";
        
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer " + access_token);
        conn.setDoOutput(true);      
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pattern_id", "p2p"));
        params.add(new BasicNameValuePair("to", payee_email));        
        params.add(new BasicNameValuePair("amount", amount));
        params.add(new BasicNameValuePair("message", message));

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();              

        int responseCode = conn.getResponseCode();
        System.out.println("Yandex_requestP2PTransfer> Response code: " + responseCode); 
        System.out.println("Yandex_requestP2PTransfer> Response message: " + conn.getResponseMessage()); 
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_requestP2PTransfer> response: " + result);
        }
        
        String JSON = result;        
        JSONObject obj = new JSONObject(JSON);
        String status = obj.getString("status");
        if (status.equals("success")) {
            String request_id = obj.getString("request_id");
            System.out.println("Yandex_requestP2PTransfer> request_id: " + request_id);
            return yandex_processP2PTransfer (request_id, payee_email, amount, message);
        } else
            return "-1";
    }
    
    private String yandex_processP2PTransfer(
            String request_id,
            String payee_email, 
            String amount, 
            String message) throws Exception { 
        
        Settings settings = new Settings();       
        String url = settings.api_url_p2p_process;
        String access_token = settings.api_url_p2p_access_token; 
        String result = null;
        String currency = "RUB";

        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer " + access_token);
        conn.setDoOutput(true);      
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("request_id", request_id));
        params.add(new BasicNameValuePair("money_source", "wallet"));

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();              

        int responseCode = conn.getResponseCode();
        System.out.println("Yandex_processP2PTransfer> Response code: " + responseCode); 
        System.out.println("Yandex_processP2PTransfer> Response message: " + conn.getResponseMessage()); 
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_processP2PTransfer> result:" + result);
            JSONObject obj = new JSONObject(result);
            if (obj.getString("status").equals("success")) {
                String tx_id = obj.getString("payment_id");
                System.out.println("Yandex_processP2PTransfer> transaction_id: " + tx_id);
                 
                MUW muw_wallet = new MUW();
                muw_wallet.updateFiatBalance(payee_email, currency, Double.valueOf(amount),  "withdrawal" /* deposit or withdrawal */);
                
                Thread.sleep(5000);
                String tx_data = yandex_getOperationDetails(tx_id);
                System.out.println("Yandex_processP2PTransfer> transaction data: " + tx_data);
                
                Utils utils = new Utils();
                utils.logFiatWithdrawalResultsFromYandex(
                    currency,
                    Double.valueOf(amount),
                    "Yandex.Money",
                    "stan1368@yandex.ru", 
                    payee_email,
                    tx_id,
                    tx_data,
                    message);
            }        
        }        

        return  result;
    } 
    
    public String yandex_getOperationDetails(String operation_id) throws Exception {
        
        Settings settings = new Settings();
        String url = settings.api_url_operation_details;
        String access_token = settings.access_token_full; 
        
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer " + access_token);
        conn.setDoOutput(true); 
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("operation_id", operation_id));
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode); 
        System.out.println("Response message: " + conn.getResponseMessage());
        if (responseCode != 200)
            return "-1";
        
        String response_content = null;
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            response_content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(response_content);
        }     
        
        return response_content;        
    }
    
    public String yandex_getOperationsHistory(
            String type, 
            String records, 
            String start_record,
            boolean show_details) throws Exception {
        
        Settings settings = new Settings();
        String url = settings.api_url_operations;
        String access_token = settings.access_token_full; 
  
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer " + access_token);
        conn.setDoOutput(true); 
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("records", records));        
        params.add(new BasicNameValuePair("details", show_details ? "true" : "false"));  
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("Yandex_getOperationsHistor> Response code: " + responseCode); 
        System.out.println("Yandex_getOperationsHistor> Response message: " + conn.getResponseMessage());
        if (responseCode != 200)
            return "-1";
        
        String response_content = null;
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            response_content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_getOperationsHistor> response_content:" + response_content);
        }        
       
        return response_content;
    }
         
    public String yandex_getTemporaryToken(String client_id, String redirect_uri, String scope) throws Exception {
                         
        Settings settings = new Settings();        
        String url = settings.api_url_temporary_access_token;

        HttpPost httppost = new HttpPost(url);        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("client_id", client_id));
        params.add(new BasicNameValuePair("response_type", "code")); // fixed value: code
        params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
        params.add(new BasicNameValuePair("scope", scope));
         
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
         
        URIBuilder query = new URIBuilder(url);
        query.addParameters(params);
     
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());
     
        CloseableHttpResponse response = null;
        String response_content;
        try {            
            response = client.execute(request);
            System.out.println(response.getStatusLine()); 
            System.out.println("yandex_GetTemporaryToken> " + response.toString()); 
            HttpEntity entity = response.getEntity(); 
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            System.out.println("yandex_GetTemporaryToken> " + response_content); 
        } finally {
            response.close();
        }                 

        return "OK";
    }  
         
    public String yandex_getInstanceId(String client_id) throws Exception {
         
        Settings settings = new Settings();
                         
        String result = null;
        String status = null;
        String instance_id = null;
        String url = settings.api_url_instanceid;
        URL myurl = new URL (url);
         
        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);        
        conn.setRequestMethod("POST");        
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("client_id", client_id)); 

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
         
        writer.flush();
        writer.close();
        os.close();

        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("GetYandexInstanceId> Response code: " + responseCode); 
        System.out.println("GetYandexInstanceId> Response message: " + conn.getResponseMessage());
         
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {            
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(result);
        }
         
        String JSON = result;        
        JSONObject obj = new JSONObject(JSON);
        status = obj.getString("status");
        System.out.println(obj.getString("status"));
         
        if (status.equals("success")) {
            instance_id = obj.getString("instance_id");
            System.out.println("GetYandexInstanceId> result:" + obj.getString("instance_id"));  
        } else {
            System.out.println("GetYandexInstanceId> instance_id:" + result); 
            return "-1";
        }
         
        return instance_id;
    } 
                 
    public String utility_MakeHiddenSubmitForm(String order_id) throws Exception {
        String url= "https://money.yandex.ru/transfer/api";
        String header = "<html><body onload=init()>";
        String footer = "</body></html>";
        String form = "";
        String script = "";
         
        String form_part1 = "<form action='" + url + "' method='POST'>";
        String form_part2 = "<input type='hidden' name='orderId' value='" + order_id + "'><br>";
        String form_part3 = "<input type='hidden' id='mysubmit' type='submit'>";
        String form_part4 = "</form>";

        script = "<script>function init() { var button = document.getElementById('mysubmit');button.form.submit();}</script>";
         
        form = header + form_part1 + form_part2 + form_part3 + form_part4 + script + footer;
        System.out.println("MakeHiddenSubmitForm> " + form);
        return form; 
    }
    
    private static String utility_getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}