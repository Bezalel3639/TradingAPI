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