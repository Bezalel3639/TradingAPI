package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
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

import io.swagger.annotations.Api;

public class MUW {
    
    boolean wallet_verification_status = true;
    
    public String getCustomerBalance(String email, String currency) throws Exception {
        
        Settings settings = new Settings();        
        String user = "db_operator";
        String database = "wallets";
        String db_collection = "";
        
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB";
                break;                
            case ("EUR"):
                db_collection = "EUR";
                break; 
            case ("HKD"):
                db_collection = "HKD";
                break; 
            case ("ILS"):
                db_collection = "ILS";
                break; 
            case ("USD"):
                db_collection = "USD";
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
        System.out.println("GetCustomerBalance> Balance: " + obj.getDouble("balance"));  
        String balance = String.valueOf(obj.getDouble("balance"));
        return balance;
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
                db_collection = "RUB";
                break;                
            case ("EUR"):
                db_collection = "EUR";
                break; 
            case ("HKD"):
                db_collection = "HKD";
                break; 
            case ("ILS"):
                db_collection = "ILS";
                break; 
            case ("USD"):
                db_collection = "USD";
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
            BasicDBObject query = new BasicDBObject(); 
            query.put("email", email);
            BasicDBObject data = new BasicDBObject(); 
            data.put("balance", type=="deposit" ? current_balance + balance_update_amount : current_balance - balance_update_amount); 
            System.out.println("UpdateFiatBalance> New balance: " + (current_balance - balance_update_amount));  
            BasicDBObject command = new BasicDBObject();
            command.put("$set", data);
            collection.updateOne(query, command);            
        }; 
        
        mongoClient.close();
    }
    
    public boolean verifyAllWallets() throws Exception { 
        
        Settings settings = new Settings();
        wallet_verification_status = true;
        String user = "db_operator";
        String db_name = "wallets";
        int NUMBEROFFIATS = 3;
        String[] fiats = {"RUB", "USD", "HKD"};
        String[] wallets = {"RUB", "USD", "HKD"};
        
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
                System.out.println("VerifyAllWallets> Currency: " + fiats[i] + ", Email: " + doc.getString("email") + ", Balance: " + doc.get("balance"));
                verifyCustomerBalance(doc.getString("email"), fiats[i]);
            }          
        }
        mongoClient.close();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("VerifyAllWallets> Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);
         
        return wallet_verification_status;
    }
    
    public void verifyCustomerBalance(String email, String currency) throws Exception { 
        Settings settings = new Settings();
        Utils utils = new Utils();
        
        String user = "db_operator";
        String database = "logs";
        String db_collection = "fiat_deposits";
        
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
        
        db_collection = "fiat_withdrawals";
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
        
        expected_balance = total_deposits - total_withdrawals;
        System.out.println("VerifyCustomerBalance> Expected balance: " + expected_balance +  " " + currency); 
        
        database = "wallets";
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB";
                break;                
            case ("EUR"):
                db_collection = "EUR";
                break; 
            case ("HKD"):
                db_collection = "HKD";
                break; 
            case ("ILS"):
                db_collection = "ILS";
                break; 
            case ("USD"):
                db_collection = "USD";
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
}
