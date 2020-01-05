package com.bezalel.trading_api;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class FiatSolutions {
    
    static class FormData
    {
        public String cps_context_id; 
        public String signature;  
        public String operationXml; 
        public String tmxSessionId;
        public String yandexuid;
        public String nst_unilabel;
    };

    public Double fiatGetRate(String symbol, String base) throws Exception {

        Settings settings = new Settings();
        String url = settings.fiat_rates_url;        
         
        HttpPost httppost = new HttpPost(url);        
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("symbols", symbol));
        params.add(new BasicNameValuePair("base", base));
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
            HttpEntity entity = response.getEntity();
            response_content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            System.out.println("FiatGetRate> :" + response_content);
        } finally {
            response.close();
        }
       
        String JSON = response_content;
         
        JSONObject obj = new JSONObject(JSON);
        JSONObject rates = obj.getJSONObject("rates");
        System.out.println(rates.toString()); 
        System.out.println(rates.getDouble(symbol)); 
        
        return rates.getDouble(symbol);
    } 
    
    public double yandex_getBalance() throws Exception {
        
        Settings settings = new Settings();
        String url = settings.api_url;
        String access_token = settings.access_token; 
       
        URL urlobj = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)urlobj.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer " + access_token);
        conn.setDoOutput(true);        

        int responseCode = conn.getResponseCode();
        System.out.println("GetYandexBalance> Response code: " + responseCode); 
        System.out.println("GetYandexBalance> Response message: " + conn.getResponseMessage());
        if (responseCode != 200)
            return -1;
        
        String response_content = null;
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            response_content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(response_content);  
        }         
   
        String JSON = response_content;        
        JSONObject obj = new JSONObject(JSON);
        System.out.println(obj.getDouble("balance")); 
        
        return obj.getDouble("balance");
    }
    
    public String yandex_getOperationsHistory(
            String type, 
            String records, 
            String start_record) throws Exception {
        
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
        params.add(new BasicNameValuePair("client_id", client_id)); // Test 3

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
        
        if (status.equals("success"))
        {
            instance_id = obj.getString("instance_id");
            System.out.println("GetYandexInstanceId> result:" + obj.getString("instance_id"));  
        }
        else
        {
            System.out.println("GetYandexInstanceId> instance_id:" + result); 
            return "-1";
        }
        
        return instance_id;
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
            System.out.println("yandex_GetTemporaryToken> " + response_content); // {"error":"invalid_request"}
        } finally {
            response.close();
        }        

        return "OK";
    } 
    
    public String yandex_requestP2PTransfer(
            String payee_account, 
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
        params.add(new BasicNameValuePair("to", payee_account));
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
        System.out.println("Response code: " + responseCode); 
        System.out.println("Response message: " + conn.getResponseMessage());
        
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
            return yandex_processP2PTransfer (request_id);
        } else
            return "-1";        
    }
    
    private String yandex_processP2PTransfer(String request_id) throws Exception { 
        
        Settings settings = new Settings();       
        String url = settings.api_url_p2p_process;
        String access_token = settings.api_url_p2p_access_token; // payment-p2p right
        String result = null;

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
        System.out.println("Response code: " + responseCode); // 200
        System.out.println("Response message: " + conn.getResponseMessage());
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_processP2PTransfer> result:" + result);
        }        
 
        return  result;
     } 
    
    public String yandex_requestPaymentFromCard(String amount) throws Exception {
        
        Settings settings = new Settings();       
        String url = settings.api_url_request_depositfromcard;
        String instance_id = settings.instanceid_depositfromcard;
        String result = null;        
     
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);        
        conn.setRequestMethod("POST");
        
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pattern_id", "p2p"));
        params.add(new BasicNameValuePair("instance_id", instance_id));
        params.add(new BasicNameValuePair("to", "410014402693803"));
        params.add(new BasicNameValuePair("amount_due", amount)); 

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
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println("Yandex_requestPaymentFromCard> response:" + result);
        }
        
        String JSON = result;        
        JSONObject obj = new JSONObject(JSON);
        String status = obj.getString("status");
        if (status.equals("success")) {
            String request_id = obj.getString("request_id");
            System.out.println("Yandex_requestPaymentFromCard> request_id: " + request_id);
            return yandex_processPaymentFromCard(request_id);
        } else
            return "-1";
    } 
    
    public String yandex_processPaymentFromCard(String request_id) throws Exception {
        
        Settings settings = new Settings();       
        String url = settings.api_url_process_depositfromcard;
        String instance_id = settings.instanceid_depositfromcard;
        String result = null;         
        
        URL myurl = new URL (url);

        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);        
        conn.setRequestMethod("POST");
        
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("instance_id", instance_id)); 
        params.add(new BasicNameValuePair("request_id", request_id));
        params.add(new BasicNameValuePair("ext_auth_success_uri", settings.yandex_redirect_url));
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
        System.out.println("Response code: " + responseCode); 
        System.out.println("Response message: " + conn.getResponseMessage());
        
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
            String cps_context_id = obj.getString("cps_context_id");
            String paymentType = obj.getString("paymentType");
            System.out.println("Yandex_processPaymentFromCard> acs_uri: " + acs_uri + ", cps_context_id: " + cps_context_id + ", paymentType: " + paymentType);
            utility_processPaymentFromCard_GetRawForm(acs_uri, cps_context_id, paymentType);

            return "See Yandex_Form.html resulting from the next call utility_ProcessPaymentFromCard_GetRawForm";
        } else
            return "-1";
    } 
    
    private void utility_processPaymentFromCard_GetRawForm(
            String acs_uri, 
            String cps_context_id, 
            String paymentType) throws Exception {
        
        String response_file = "Yandex_Form.html";
        URL myurl = new URL (acs_uri);
        
        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);        
        conn.setRequestMethod("POST");
        
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("cps_context_id", cps_context_id));
        params.add(new BasicNameValuePair("paymentType", paymentType));
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
        
        String result = null;
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            result = reader.lines().collect(Collectors.joining(System.lineSeparator()));
             
            File file = new File(response_file); // Yandex_Form.html

            FileWriter fs = new FileWriter(file);
            fs.write(result);
            fs.close();
        }    
    }   
  
    public void utility_createCardInputForm() throws Exception {
       String response_filename = "Yandex_Form.html";
       String response_file = "file:///C://Trading//Eclipse EE//Workspace//TradingAPI//" + response_filename;
       String form_template = "file:///C://Trading//Eclipse EE//Workspace//TradingAPI//About//Yandex_CardInputForm_Simulated.htm";
       
       FormData fd = new FormData();       
       fd = utility_findHTMLElements(response_file);
       
       String yandexHTML = utility_readFile (form_template);
       Document doc = Jsoup.parse(yandexHTML);        
 
       Elements el = doc.select("input[name=cps_context_id]");
       el.attr("value", fd.cps_context_id);
       
       el = doc.select("input[name=skr_successUrl]");
       el.attr("value", "https://f6f687ad.ngrok.io/TradingAPI/Yandex_Servlet"); // TODO
       
       el = doc.select("input[name=skr_errorUrl]");
       el.attr("value", "http://example.com/fail"); // TODO
       
       el = doc.select("input[name=skr_sum]");
       el.attr("value", "1.02"); // TODO
       
       el = doc.select("input[name=cps_provider]");
       el.attr("value", "default"); // TODO
       
       el = doc.select("input[name=amount]");
       el.attr("value", "1.00"); // TODO
       
       el = doc.select("input[name=cps-source]");
       el.attr("value", "quickpay"); 
       
       el = doc.select("input[name=signature]");
       el.attr("value", fd.signature); 
       
       el = doc.select("input[name=operationXml]");
       el.attr("value", fd.operationXml); 
       
       el = doc.select("input[name=paymentType]");
       el.attr("value", "FC");
       
       el = doc.select("input[name=nst_unilabel]");
       el.attr("value", fd.nst_unilabel);
       
       el = doc.select("input[name=yandexuid]");
       el.attr("value", fd.yandexuid);       
      
       el = doc.select("input[name=tmxSessionId]");
       el.attr("value", fd.tmxSessionId);
       
       File file = new File("Yandex_NewFormTest.html");

       FileWriter fs = new FileWriter(file);
       fs.write(doc.toString());
       fs.close(); 
       
       if (Desktop.isDesktopSupported()) {
           Desktop.getDesktop().browse(file.toURI()); 
       } else {
           System.out.println("Desktop is not supported!"); 
           Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " +  file.toURI());
       }
   }
   
   public static FormData utility_findHTMLElements(String filepath) throws Exception {

       String yandexHTML = utility_readFile (filepath);     
       FormData fd = new FormData();
       Document doc = Jsoup.parse(yandexHTML);            
           
       Elements el = doc.select("input[name=cps_context_id]");
       System.out.println("FindHTMLElement> cps_context_id: " + el.attr("value").toString());
       fd.cps_context_id = el.attr("value").toString();
       
       el = doc.select("input[name=signature]");
       System.out.println("FindHTMLElement> signature: " + el.attr("value").toString()); // 8a1da9de38cff742fe873555aebc454382dcc5c2
       fd.signature = el.attr("value").toString();
               
       el = doc.select("input[name=operationXml]");
       System.out.println("FindHTMLElement> operationXml: " + el.attr("value").toString());
       fd.operationXml = el.attr("value").toString();
       
       el = doc.select("input[name=tmxSessionId]");
       System.out.println("FindHTMLElement> tmxSessionId: " + el.attr("value").toString()); // 2A887D814BEBF50498953F39CAD22C44
       fd.tmxSessionId = el.attr("value").toString();
       
       el = doc.select("p[style]");            
       System.out.println("FindHTMLElement> session_id: " + el.toString());
       
       el = doc.select("input[name=nst_unilabel]");
       System.out.println("nst_unilabelFindHTMLElement> nst_unilabel: " + el.attr("value").toString()); // 258ec3ad-0009-5000-8000-00004207060e
       fd.nst_unilabel = el.attr("value").toString();
       
       // CONSTANTS?
       el = doc.select("input[name=yandexuid]");
       System.out.println("FindHTMLElement> yandexuid: " + el.attr("value").toString()); // 1287843061576146657
       fd.yandexuid = el.attr("value").toString();
       
       el = doc.select("input[name=skr_env]");
       System.out.println("FindHTMLElement> skr_env: " + el.attr("value").toString());
       
       return fd;
   }
   
   public static String utility_readFile(String filepath) throws Exception {
       URL url = new URL(filepath); 
       BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

       String line;
       String result = null;
       while ((line = reader.readLine()) != null) {
           result = result + line;
       }
       reader.close();  
   
       return result;
   }
}