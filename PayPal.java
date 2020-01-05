package com.bezalel.trading_api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import org.json.JSONObject;
import java.util.Iterator;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.paypal.api.payments.Links;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.net.URI;

public class PayPal {

    public String paypal_Deposit(String amount, String symbol) {

        String value = "";		
        Settings settings = new Settings();

        Amount amount_object = new Amount();
        amount_object.setCurrency(symbol);
        amount_object.setTotal(amount);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount_object);
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("https://localhost:3000/cancel");
        
        String OS = System.getProperty("os.name");	
        if (OS.equals("Windows 10")) {
            redirectUrls.setReturnUrl(settings.baseURL_Windows10 + "/TradingAPI/PayPal_Servlet");
        }
        else if (OS.equals("Linux")) {
            redirectUrls.setReturnUrl(settings.baseURL_Linux + "/PayPal_Servlet");
        }
        else {
            System.out.println("paypal_Deposit> The OS is not supported");
            return "Cound not set redirect URL";
        }
    
        payment.setRedirectUrls(redirectUrls);	
        
        try {
            APIContext apiContext = new APIContext(settings.clientId, settings.clientSecret, "sandbox");
            System.out.println("paypal_Deposit> Access token: " + apiContext.fetchAccessToken());
            Payment createdPayment = payment.create(apiContext);
            System.out.println(createdPayment.toString());
            
            Iterator<Links> links = createdPayment.getLinks().iterator();
            
            // Get approval_url and redirect the customer to approval_url
            while (links.hasNext()) { 		    
                Links link = links.next();
                if (link.getRel().equalsIgnoreCase("approval_url")) {
                    System.out.println(link.getHref());
                    System.out.println("paypal_Deposit> : Match found");
                    value = link.getHref();

                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(link.getHref()));
                    } else {
                        System.out.println("paypal_Deposit> : desktop is not supported");  
                    }
                }
            }
        } catch (PayPalRESTException e) {
             System.err.println("paypal_Deposit> PayPalRESTException: " + e.getDetails());
        } catch (Exception ex) {
             System.out.println("paypal_Deposit> Exception: " + ex.toString());
        }
        
        if (OS.equals("Windows 10")) {
            try {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " +  value); // OK
            }
            catch (Exception ex)
            {
                System.out.println("Runtime.getRuntime().exec error");	
            }
        } else if (OS.equals("Linux")) {
            try {
                Desktop.getDesktop().browse(new URI(value));
            } catch (Exception ex) {
                System.out.println("paypal_Deposit> Desktop.isDesktopSupported: " + Desktop.isDesktopSupported()); // false
            }
        }
        
        return value;
    }
    
    public String paypal_Withdraw(String receiver, String amount, String symbol, String accesstoken, String sender_batch_id) throws Exception {
         
        Settings settings = new Settings();

        String email_subject = "";
        String email_message = "";
        String recipient_type = "EMAIL";
        String currency = "RUB";
        String note = "Test sandbox withdrawal to email";
        String sender_item_id = "12272019_2";
        
        // Alternate notification method
        String country_code = "X";
        String national_number = "XXX711XX49"; 
        
        String content = "{\"sender_batch_header\": { \"sender_batch_id\": " + sender_batch_id + ", \"email_subject\": \"You have a withdrawal result!\", \"email_message\": \"You have a withdrawal result! Thanks for using our service!\" }, \"items\": [ { \"recipient_type\": \"EMAIL\",  \"amount\": {  \"value\": " + amount + ", \"currency\": \"RUB\" }, \"note\": \"Test sandbox withdrawal to email\", \"sender_item_id\": \"12312019\", \"receiver\": \"sb-sxwqb793030@personal.example.com\",  \"alternate_notification_method\": { \"phone\": { \"country_code\": \"7\", \"national_number\": \"9687115049\" } } } ] }";
        String authorization = "Bearer " + accesstoken;        
        
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
             .url(settings.url_paypal_withdraw)
             .method("POST", body)
             .addHeader("Content-Type", "application/json")
             .addHeader("Authorization", authorization)
             .build();
       Response response = client.newCall(request).execute();
       
       String raw_result = response.body().string();
       System.out.println("paypal_Withdraw> body: " + response.toString());        
       System.out.println("paypal_Withdraw> result: " + raw_result); 
       
       String[] values = response.toString().split(","); 
       System.out.println("paypal_Withdraw> array: " + Arrays.toString(values));
       System.out.println("paypal_Withdraw> values: " + values);
       
       String raw_message = java.net.URLDecoder.decode(values[2], StandardCharsets.UTF_8.name());
       System.out.println(raw_message); 
       String[] message =raw_message.split("=");
       String msg = message[1];
       System.out.println("Result: " + msg); 
       
       if (msg.equals("Unauthorized"))
           return "Token is invalid";
       if (!msg.equals("Created"))
           return "-1";  // Something went wrong!      
    
       return "Success";
    }
    
    public String paypal_getBalance(String currency) throws Exception {
        
        Settings settings = new Settings();
        String content = "USER=" + settings.test_user + "&PWD=" + settings.test_user_password + 
            "&SIGNATURE=" + settings.test_user_signature + 
            "&VERSION=109.0&METHOD=GetBalance&RETURNALLCURRENCIES=1";
        String raw_result = null;
        
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, content);
    
        Request request = new Request.Builder()
            .url(settings.url_paypal_balance)
            .method("POST", body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build();       
        
        Response response = client.newCall(request).execute();
        raw_result = response.body().string();
        System.out.println(response.toString());        
        System.out.println(raw_result);  
 
        String[] values = raw_result.split("&"); 
        String raw_array = java.net.URLDecoder.decode(Arrays.toString(values), StandardCharsets.UTF_8.name()); 
        System.out.println(Arrays.toString(values));
 
        // Current currency order: RUB, EUR, HKD, ILS, USD.
        int index;
        switch (currency)
        {
            case ("RUB"):
                index = 0;
                break;                
            case ("EUR"):
                index = 1;
                break; 
            case ("HKD"):
                index = 2;
                break; 
            case ("ILS"):
                index = 3;
                break; 
            case ("USD"):
                index = 4;
                break;
            default:
                index = -1; // not supported
        }
            
        String raw_balance = java.net.URLDecoder.decode(values[index], StandardCharsets.UTF_8.name());
        System.out.println(raw_balance); 
        String[] balance = raw_balance.split("=");
        String result = balance[1];
        System.out.println("Balance: " + balance[1] + " " + currency); 
        
        return result;
    }
    
    public String utility_getAccessToken(String password)  throws Exception {
            
        String result = null;
        Settings settings = new Settings();
        
        // Validate password
        String true_password = settings.paypal_p2p_password;
        if (!true_password.equals(password))
            return "Wrong password!";
        
        String raw_auth = settings.clientId + ":" + settings.clientSecret;
        String authorization = null;
        
        byte[] encodedBytes = Base64.getEncoder().encode(raw_auth.getBytes());
        authorization = new String(encodedBytes);
        System.out.println("utility_getAccessToken> authorization: " + authorization);
 
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials");
        Request request = new Request.Builder()
            .url(settings.url_paypal_gettoken)
            .method("POST", body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", "Basic " + authorization) 
            .build();
       Response response = client.newCall(request).execute();
         
       String raw_result = response.body().string();
       System.out.println("utility_getAccessToken> result: " + response.toString());        
       System.out.println("utility_getAccessToken> response body: " + raw_result);
       
       JSONObject obj = new JSONObject(raw_result);
       result = obj.getString("access_token");
       System.out.println("utility_getAccessToken> access_token: " + result ); 
       System.out.println("utility_getAccessToken> expires_in: " + obj.getInt("expires_in"));
       System.out.println("utility_getAccessToken> app_id: " + obj.getString("app_id"));
       System.out.println("utility_getAccessToken> nonce: " + obj.getString("nonce"));  
       System.out.println("utility_getAccessToken> scope: " + obj.getString("scope")); 
       
       return result ;   
    }
    
    // Reserved, not used at the moment
    public static String utility_getRandomString() {
        
        int leftLimit = 97; 
        int rightLimit = 122; 
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) 
              (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();     
        System.out.println(generatedString); 
        
        return generatedString;
    }
}



