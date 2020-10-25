package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bson.Document;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/Yandex_Servlet")
public class Yandex_Servlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
       
    public Yandex_Servlet() {
        super();
    }

    protected void doGet(
            HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {
        
        Settings settings = new Settings();
        Utils  utils = new  Utils();
        System.out.println("doGet> Yandex_Servlet()");
        String orderlabel = request.getParameter("label");
        System.out.println("Yandex_Servlet> label: " + orderlabel);
        String amount = request.getParameter("amount");
        System.out.println("Yandex_Servlet> amount: " + amount);
        String commission = request.getParameter("commission");
        System.out.println("Yandex_Servlet> commission: " + commission);
        String emailfrom = request.getParameter("emailfrom");
        System.out.println("Yandex_Servlet> emailfrom: " + emailfrom);

        JSONObject objSearchResult = null;        
        
        String currency = "RUB";
        String emailto = "stan1368@yandex.ru";
        
        boolean isLabelExist = false;        
        for (String element : Settings.tx_labels){
            if (element.contains(orderlabel)){
                  System.out.println("Yandex_Servlet> Element is found: " + element); // OK
                  isLabelExist = true;
            } 
        }

        if (!isLabelExist) Settings.tx_labels.add(orderlabel);
        
        if (!isLabelExist) {         
            try {
                Thread.sleep(5000);
                objSearchResult = yandex_getTXData(orderlabel); 
                if (objSearchResult.get("status").equals("200")) { 
                    System.out.println("Yandex_Servlet> The TX data is found: " + objSearchResult.get("data").toString());
                    
                    updateFiatBalance(
                            emailfrom, 
                            currency, 
                            Double.valueOf(amount));
                    
                    JSONObject objTXData = (JSONObject)objSearchResult.get("data");
                    utils.logFiatDepositResultsFromYandex(
                        currency,
                        Double.valueOf(amount),
                        Double.valueOf(commission),
                        emailfrom, 
                        emailto, 
                        orderlabel,
                        objTXData.getString("operation_id"), 
                        objSearchResult.get("data").toString(),
                        "");
                } else {
                    System.out.println("Yandex_Servlet> The TX data for label "  + orderlabel + " is not found!");                    
                }
            } catch (Exception e1) {
                System.out.println("Yandex_Servlet> Exception occurred!");
                e1.printStackTrace();
            }
        }

        PrintWriter out = response.getWriter();
        System.out.println("doGet> response: " + response.toString());
        String cps_card = request.getParameter("cps_card");
        if (cps_card == null)
        {
            String code = request.getParameter("code");
            out.println(code);
            
            if (code != null) {
                System.out.println("Code value: " + code);
                try {
                    Yandex_GetAccessToken(code);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else { /* deposit with banking card */            
            String orderN = request.getParameter("orderN");
            System.out.println("Yandex_Servlet> doGet: orderN: " + orderN);
            System.out.println("Yandex_Servlet> doGet: cps_card: " + cps_card);
            String sum = request.getParameter("sum");
            System.out.println("Yandex_Servlet> doGet: sum: " + sum);
        }
        out.close(); 
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Test only
        System.out.println("doPost> ...");
        Enumeration paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()) {
            System.out.println("doPost> " + (String)paramNames.nextElement());
        }
        doGet(request, response);
    }
    
    public void Yandex_GetAccessToken(String code) throws Exception {
        
        System.out.println("Yandex_GetAccessToken> " + code);
        
        Settings settings = new Settings();
        String url = settings.api_url_access_token;
        String client_id = settings.yandex_client_id;
        String redirect_url = settings.yandex_redirect_url;        
        
        String query = String.format("code=%s&client_id=%s&grant_type=%s&redirect_uri=%s", 
                URLEncoder.encode(code, "UTF-8"), 
                URLEncoder.encode(client_id, "UTF-8"),
                URLEncoder.encode("authorization_code", "UTF-8"),
                URLEncoder.encode(redirect_url, "UTF-8"));
        System.out.println("Yandex_GetAccessToken> query: " + query);       
        
        URL myurl = new URL (url + "?" + query);
        System.out.println("Yandex_GetAccessToken> request URL: " + myurl);
        HttpURLConnection conn = (HttpURLConnection)myurl.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        
        try(OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(query);
        }
        
        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode); 
        System.out.println("Response message: " + conn.getResponseMessage());
        
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            System.out.println(reader.lines().collect(Collectors.joining(System.lineSeparator())));
        }        
    }
    
    public JSONObject yandex_getTXData(String label) throws Exception {
        
        String type = null;
        String start_record = null;
        String records = "1"; 
        
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
        params.add(new BasicNameValuePair("details", "true"));  
        params.add(new BasicNameValuePair("label", label));
        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(utility_getQuery(params));
        
        writer.flush();
        writer.close();
        os.close();

        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode); 
        System.out.println("Response message: " + conn.getResponseMessage());
        
        String response_content = null;
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), Charset.forName("utf-8")))) {
            response_content = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(response_content);     
        }
    
        String JSON = response_content; 
        JSONObject obj = new JSONObject(JSON);
        JSONArray array_obj = obj.getJSONArray("operations");       
       
        JSONObject result = null;
        if (array_obj.length() != 0) {
            System.out.println("yandex_getTXData> The TX is found!");
            JSONObject objTXData = array_obj.getJSONObject(0);
            result = new JSONObject(); 
            result.put("status", "200");
            result.put("data", objTXData);
            return result; 
        } 
        result = new JSONObject(); 
        result.put("status", "-1");
        return result;
    } 
    
    private String utility_getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (NameValuePair pair : params) {
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
    
    protected void updateFiatBalance(
            String email, 
            String currency, 
            double balance_update_amount) throws Exception { 
            
        Settings settings = new Settings();
        String user = "db_operator";
        String db_name = "wallets";
        String db_collection = "";
        double current_balance = 0.0;
         
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB";
                break;                
            case ("EUR"):
                db_collection = "EUR"; // reserved
                break; 
            case ("HKD"):
                db_collection = "HKD"; // reserved
                break; 
            case ("ILS"):
                db_collection = "ILS"; // reserved
                break; 
            case ("USD"):
                db_collection = "USD"; // reserved
                break;
        }        
        
        MongoCredential credential = MongoCredential.createCredential(
            user, db_name, 
            settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(
            Settings.mongodb_host, 
            settings.mongodb_port),
            Arrays.asList(credential));
             
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(db_collection);
        
        Document document = collection.find(eq("email", email)).first();
         
        if (document == null) { 
            System.out.println("Servlet> UpdateFiatBalance> Nothing was found in DB");
        } else { 
            System.out.println("Servlet> UpdateFiatBalance> A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println(obj.toString()); 
            current_balance = obj.getDouble("balance");
            System.out.println("Servlet> UpdateFiatBalance> Current balance: " + current_balance);  
            
            // Update balance
            BasicDBObject query = new BasicDBObject(); 
            query.put("email", email);
            BasicDBObject data = new BasicDBObject(); 
            data.put("balance", current_balance + balance_update_amount); 
            System.out.println("Servlet> UpdateFiatBalance> New balance: " + current_balance + balance_update_amount);  
            BasicDBObject command = new BasicDBObject();
            command.put("$set", data);
            collection.updateOne(query, command);            
        }; 
        
        mongoClient.close();
    }
}