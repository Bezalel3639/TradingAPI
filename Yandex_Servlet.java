package com.bezalel.trading_api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter; 
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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
        System.out.println("doGet> Yandex_Servlet()");

        PrintWriter out = response.getWriter();
        //out.close(); 
        
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
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
}





