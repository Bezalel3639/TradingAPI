package com.bezalel.trading_api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Iterator;
import org.bson.Document;
import org.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.api.payments.Links;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.nio.charset.StandardCharsets;

import static com.mongodb.client.model.Filters.eq;

import java.awt.Desktop;
import java.net.URI;

public class PayPal {
    
    boolean wallet_verification_status = true;

    public String paypal_Deposit(String email, String amount, String symbol) {

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
        PayerInfo pi = new PayerInfo();
        pi.setEmail(email);
        payer.setPayerInfo(pi);

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
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " +  value); 
            }
            catch (Exception ex)
            {
                System.out.println("Runtime.getRuntime().exec error");	
            }
        } else if (OS.equals("Linux")) {
            try {
                Desktop.getDesktop().browse(new URI(value));
            } catch (Exception ex) {
                System.out.println("paypal_Deposit> Desktop.isDesktopSupported: " + Desktop.isDesktopSupported()); 
            }
        }
        
        return value;
    }
    
    public String paypal_Withdraw(String receiver, String amount, String symbol, String accesstoken) throws Exception {
         
        Settings settings = new Settings();
        Utils utils = new Utils();

        String email_subject = "New subject";
        String email_message = "New email message";
        String recipient_type = "EMAIL";
        String note = "New note";
        String sender_item_id = "12272019_2";
        String sender_batch_id = utils.utility_getRandomString();  
        System.out.println("Paypal_Withdraw> sender_batch_id: " + sender_batch_id);
        
        // Alternate notification method
        String country_code = "7";
        String national_number = "9687115049"; 
        
        String content = "{\"sender_batch_header\": { \"sender_batch_id\": " + sender_batch_id + ", \"email_subject\": " + "\"" + email_subject + "\"" + ", \"email_message\": "+ "\"" + email_message + "\"" + " }, \"items\": [ { \"recipient_type\": \"EMAIL\",  \"amount\": {  \"value\": " + amount + ", \"currency\": " + "\"" + symbol + "\"" + " }, \"note\": " + "\"" + note + "\"" + ", \"sender_item_id\": \"12312019\", \"receiver\": " + "\"" + receiver + "\"" + ",  \"alternate_notification_method\": { \"phone\": { \"country_code\": \"7\", \"national_number\": \"9687115049\" } } } ] }";
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
        System.out.println("Paypal_Withdraw> body: " + response.toString());        
        System.out.println("Paypal_Withdraw> result: " + raw_result); 

        String[] values = response.toString().split(","); 
        System.out.println("Paypal_Withdraw> array: " + Arrays.toString(values));
        System.out.println("Paypal_Withdraw> values: " + values);

        String raw_message = java.net.URLDecoder.decode(values[2], StandardCharsets.UTF_8.name());
        System.out.println(raw_message); 
        String[] message =raw_message.split("=");
        String msg = message[1];
        System.out.println("Paypal_Withdraw> Result: " + msg); 

        if (msg.equals("Unauthorized"))
            return "Token is invalid";
        if (!msg.equals("Created"))
            return "-1"; 
       
       // Log results
       String notes = "";
       utils.logFiatWithdrawalResults(
           symbol, 
           Double.valueOf(amount),
           "PayPal", 
           settings.email_heap, 
           receiver, 
           raw_result,
           notes);
       
       // Update customer balance
       updateFiatBalance(receiver, symbol, Double.valueOf(amount),  "withdrawal" /* deposit or withdrawal */);
  
       return "Success";
    }	
    
    // Heap Balance
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
                index = -1;
        }
            
        String raw_balance = java.net.URLDecoder.decode(values[index], StandardCharsets.UTF_8.name());
        System.out.println(raw_balance); 
        String[] balance = raw_balance.split("=");
        String result = balance[1];
        System.out.println("Balance: " + balance[1] + " " + currency); 
        
        return result;
    }
    
    public String paypal_getCustomerBalance(String email, String currency) throws Exception {
        
        Settings settings = new Settings();        
        String user = "db_operator";
        String database = "wallets";
        String db_collection = "";
        
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB (sandbox)";
                break;                
            case ("EUR"):
                db_collection = "EUR (sandbox)";
                break; 
            case ("HKD"):
                db_collection = "HKD (sandbox)";
                break; 
            case ("ILS"):
                db_collection = "ILS (sandbox)";
                break; 
            case ("USD"):
                db_collection = "USD (sandbox)";
                break;
        }
        
        MongoCredential credential = MongoCredential.createCredential(user, database, settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(Settings.mongodb_host, settings.mongodb_port),
                Arrays.asList(credential));
             
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(db_collection);        

        Document document = collection.find(eq("email", email)).first();
        if (document == null) {             
            System.out.println("GetCustomerBalance> Nothing was found in DB");
            mongoClient.close();
            return "-1";
        }; 
        mongoClient.close();
         
        JSONObject obj = new JSONObject(document.toJson());       
        System.out.println("GetCustomerBalance> Balance: " + obj.getDouble("balance"));  // OK, Balance: 243.89
        String balance = String.valueOf(obj.getDouble("balance"));
        return balance;
    }
    
    public String paypal_getCustomerBalanceReserved(String email, String currency) throws Exception {        
        Settings settings = new Settings();
        
        String user = "db_operator";
        String database = "logs";
        String db_collection = "sandbox_fiat_deposits";

        MongoCredential credential = MongoCredential.createCredential(user, database, settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(settings.mongodb_host, settings.mongodb_port),
            Arrays.asList(credential));
             
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(db_collection);
               
        Document document =  collection.aggregate(
            Arrays.asList(
                Aggregates.match(Filters.eq("currency", currency)),
                Aggregates.match(Filters.eq("emailfrom", email)),
                Aggregates.group("$emailfrom", Accumulators.sum("total_deposits", "$amount"), Accumulators.sum("total_fees", "$fee"))
            )).first(); 
        mongoClient.close();
        
        if (document == null) { 
            System.out.println("Nothing was found in DB");
            return "-1";
        } else { 
            System.out.println("A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println(obj.toString()); 
            return obj.toString();
        }
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
    
    protected void updateFiatBalance(
            String email, 
            String currency, 
            double balance_update_amount, 
            String type /* deposit or withdrawal */) throws Exception { 
            
        Settings settings = new Settings();
        String user = "db_operator";
        String db_name = "wallets";
        String db_collection = "";
        double current_balance = 0.0;
         
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB (sandbox)";
                break;                
            case ("EUR"):
                db_collection = "EUR (sandbox)";
                break; 
            case ("HKD"):
                db_collection = "HKD (sandbox)";
                break; 
            case ("ILS"):
                db_collection = "ILS (sandbox)";
                break; 
            case ("USD"):
                db_collection = "USD (sandbox)";
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
            System.out.println("UpdateFiatBalance> Nothing was found in DB");
        } else { 
            System.out.println("UpdateFiatBalance> A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println(obj.toString()); 
            current_balance = obj.getDouble("balance");
            System.out.println("UpdateFiatBalance> Current balance: " + current_balance);  
            
            // Update balance
            BasicDBObject query = new BasicDBObject(); // query object
            query.put("email", email);
            BasicDBObject data = new BasicDBObject(); // update object
            data.put("balance", type=="deposit" ? current_balance + balance_update_amount : current_balance - balance_update_amount); 
            System.out.println("UpdateFiatBalance> New balance: " + (current_balance - balance_update_amount));  
            BasicDBObject command = new BasicDBObject();
            command.put("$set", data);
            collection.updateOne(query, command);            
        }; 
        
        mongoClient.close();
    }
    
    public boolean verifyAllPayPalWallets() throws Exception { 
        
        Settings settings = new Settings();
        wallet_verification_status = true;
        String user = "db_operator";
        String db_name = "wallets";
        int NUMBEROFFIATS = 5;
        String[] fiats = {"RUB", "EUR", "USD", "HKD", "ILS"};
        String[] wallets = {"RUB (sandbox)", "EUR (sandbox)", "USD (sandbox)", "HKD (sandbox)", "ILS (sandbox)"};
        
        MongoCredential credential = MongoCredential.createCredential(user, db_name, settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(settings.mongodb_host, settings.mongodb_port),
                Arrays.asList(credential));
             
        MongoDatabase db = mongoClient.getDatabase(db_name);
        
        long startTime = System.currentTimeMillis();
        for (int i=0; i < NUMBEROFFIATS; i++) {
            MongoCollection<Document> collection = db.getCollection(wallets[i]);            
            MongoCursor<Document> cursor = collection.find().iterator(); 
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                System.out.println("VerifyAllPayPalWallets> Currency: " + fiats[i] + ", Email: " + doc.getString("email") + ", Balance: " + doc.get("balance"));
                verifyCustomerBalance(doc.getString("email"), fiats[i]);
            }          
        }
        mongoClient.close();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("VerifyAllPayPalWallets> Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);
         
        return wallet_verification_status;
    }
    
    public void verifyCustomerBalance(String email, String currency) throws Exception { 
        Settings settings = new Settings();
        Utils utils = new Utils();
        
        String user = "db_operator";
        String database = "logs";
        String db_collection = "sandbox_fiat_deposits";
        
        double total_deposits = 0;
        double total_deposit_fees = 0;
        double total_withdrawals = 0;
        double expected_balance = 0;
        double balance = 0;

        MongoCredential credential = MongoCredential.createCredential(user, database, settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(settings.mongodb_host, settings.mongodb_port),
            Arrays.asList(credential));
             
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(db_collection);
               
        Document document =  collection.aggregate(
            Arrays.asList(
                Aggregates.match(Filters.eq("currency", currency)),
                Aggregates.match(Filters.eq("emailfrom", email)),
                Aggregates.group("$emailfrom", Accumulators.sum("total_deposits", "$amount"), Accumulators.sum("total_fees", "$fee"))
            )).first(); 
        
        if (document == null) { 
            System.out.println("VerifyCustomerBalance> Nothing was found in DB: "  +database + ", Collection: " + db_collection);
         } else { 
            System.out.println("VerifyCustomerBalance> A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println("VerifyCustomerBalance> " + obj.toString());            
             total_deposits = obj.getDouble("total_deposits");
            total_deposit_fees = obj.getDouble("total_fees");
        }
        
        db_collection = "sandbox_fiat_withdrawals";
        collection = db.getCollection(db_collection);
                
        document =  collection.aggregate(
                Arrays.asList(
                    Aggregates.match(Filters.eq("currency", currency)),
                    Aggregates.match(Filters.eq("emailto", email)),
                    Aggregates.group("$emailto", Accumulators.sum("total_withdrawals", "$amount"))
                 )).first(); 
        mongoClient.close();
                    
        if (document == null) { 
            System.out.println("VerifyCustomerBalance> Nothing was found in DB: "  +database + ", Collection: " + db_collection);
        } else { 
            System.out.println("VerifyCustomerBalance> A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println(obj.toString()); 
            total_withdrawals = obj.getDouble("total_withdrawals");
        }
        
        expected_balance = total_deposits -  total_deposit_fees - total_withdrawals;
        System.out.println("VerifyCustomerBalance> Expected balance: " + expected_balance +  " " + currency); // Expected balance: 243.89 RUB
        
        database = "wallets";
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB (sandbox)";
                break;                
            case ("EUR"):
                db_collection = "EUR (sandbox)";
                break; 
            case ("HKD"):
                db_collection = "HKD (sandbox)";
                break; 
            case ("ILS"):
                db_collection = "ILS (sandbox)";
                break; 
            case ("USD"):
                db_collection = "USD (sandbox)";
                break;
        }
        credential = MongoCredential.createCredential(user, database, settings.password.toCharArray());
        mongoClient = new MongoClient(new ServerAddress(settings.mongodb_host, settings.mongodb_port),
            Arrays.asList(credential));
        db = mongoClient.getDatabase(database);
        collection = db.getCollection(db_collection);
        document = collection.find(eq("email", email)).first();
        if (document == null) {             
            System.out.println("VerifyCustomerBalance> Nothing was found in DB: "  +database + ", Collection: " + db_collection);
        } else { 
            System.out.println("VerifyCustomerBalance> A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println(obj.toString()); 
            balance = obj.getDouble("balance");
            System.out.println("VerifyCustomerBalance> Balance: " + balance +  " " + currency); // OK: Balance: 243.89 RUB
        } 
        mongoClient.close();        
        
        if (utils.round(expected_balance, 2) != utils.round(balance, 2)) {
            System.out.println("VerifyCustomerBalance> Error is found! The balance for the " + 
                currency + " account of the customer " + email + " is not correct! Expected balance: " + 
                expected_balance +  " " + currency + ", Balance: " + balance + " " + currency);
            wallet_verification_status = false;
        }
    }        
}