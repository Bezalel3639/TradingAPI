package com.bezalel.trading_api;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.MongoCollection;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BackOffice {
    
    Settings settings = new Settings();

    String db_name = "users";
    MongoCredential credential = MongoCredential.createCredential(
            settings.user, 
            db_name, 
            settings.password.toCharArray());
    MongoClient mongoClient = new MongoClient(new ServerAddress(
            settings.mongodb_host, 
            settings.mongodb_port),
            Arrays.asList(credential));
    
    public class UserValidation {
        public boolean isvalidated;
        public boolean isadmin; 
        public String code;
        public String message;
    };
    
    public String getUserAssets(String user) throws Exception {       
        
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("investors");

        Document document = collection.find(eq("user", user)).first();
        if (document == null) {             
            System.out.println("Nothing was found in DB");
            return "404"; 
        }; 
         
        JSONObject obj = new JSONObject(document.toJson());       
        JSONArray objnext = obj.getJSONArray("assets");
        System.out.println(objnext.toString());
        String data= objnext.toString();
        mongoClient.close();
  
        return data;
    }
    
    public String addPortfolioAsset(String user, String asset, String address) throws Exception {     
        
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("investors");
        
        // Query object
        BasicDBObject query = new BasicDBObject();
        query.put("user", user);
        
        // Update object
        DBObject object = new BasicDBObject();
        object.put("name", asset);
        object.put("address", address);
        DBObject newitem = new BasicDBObject("assets", object);
        BasicDBObject update = new BasicDBObject("$push", newitem);      
        
        collection.updateOne(query, update);
        mongoClient.close();
        
        return "The asset was added";
    } 
    
    public String addBalanceAsset(String user, String asset, String address) throws Exception {     
        
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("balances_sandbox");
        
        // Query object
        BasicDBObject query = new BasicDBObject();
        query.put("user", user);
        
        // Update object
        DBObject object = new BasicDBObject();
        object.put("name", asset);
        object.put("address", address);
        DBObject newitem = new BasicDBObject("assets", object);
        BasicDBObject update = new BasicDBObject("$push", newitem);      
        
        collection.updateOne(query, update);
        mongoClient.close();
        
        return "The asset was added";
    } 
    
    public String removeAsset(String user, String asset, String address) throws Exception {
        
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("investors");
       
        // Query object
        BasicDBObject query = new BasicDBObject();
        query.put("user", user);
        
        // Update object with $pull
        DBObject object = new BasicDBObject();
        object.put("name", asset);
        object.put("address", address); 
        DBObject removeitem = new BasicDBObject("assets", object);
        BasicDBObject update = new BasicDBObject("$pull", removeitem); 
        
        collection.updateOne(query, update);
        mongoClient.close();
        
        return "The asset was removed";
    }
    
    public String updateAssetData(String user, int index, String newdata) throws Exception {
         
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("investors");
       
        // Query object
        BasicDBObject query = new BasicDBObject();
        query.put("user", user);
        
        // Update object
        BasicDBObject data = new BasicDBObject();
        data.put("assets." + index + ".address", newdata); 

        BasicDBObject command = new BasicDBObject();
        command.put("$set", data);

        collection.updateOne(query, command); 
        mongoClient.close();
        
        return "The data was updated";
    } 
    
    public String getUserBalanceAssets(String user) throws Exception {    
        
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("balances");

        Document document = collection.find(eq("user", user)).first();
        if (document == null) {             
            System.out.println("Nothing was found in DB");
            //return "404"; 
            return "The user is not found!"; 
        }; 
         
        JSONObject obj = new JSONObject(document.toJson());       
        JSONArray objnext = obj.getJSONArray("assets");
        System.out.println(objnext.toString());
        String data= objnext.toString();
        mongoClient.close();
  
        return data;
    }
    
    public String getUserSandBoxBalanceAssets(String user) throws Exception {    
        
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("balances_sandbox");

        Document document = collection.find(eq("user", user)).first();
        if (document == null) {             
            System.out.println("Nothing was found in DB");
            return "The user is not found!"; 
        }; 
         
        JSONObject obj = new JSONObject(document.toJson());       
        JSONArray objnext = obj.getJSONArray("assets");
        System.out.println(objnext.toString());
        String data= objnext.toString();
        mongoClient.close();
  
        return data;
    }
    
    public UserValidation validateUser(String email, String password) throws Exception {
        
        String db_collection = "login_accounts";
        UserValidation uv = new UserValidation();
             
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(db_collection);

        Bson filter = Filters.and(
            Filters.eq("email", email),
            Filters.eq("password", password));
        Document document = collection.find(filter).first();
        
        if (document == null) { 
            System.out.println("Nothing was found in DB"); 
            uv.isvalidated = false;
            // Run query a second time to get details about missing data
            document = collection.find(eq("email", email)).first();
            if (document == null) {
                System.out.println("Email not was found in DB");
                uv.code = ""; 
                uv.message = "Email not was found in DB";
                return uv;
            } else { 
                System.out.println("Email was found in the DB, but password is wrong!");                
                uv.code = ""; 
                uv.message = "Email was found in the DB, but password is wrong!";
                return uv;
            }             
        } else { 
            System.out.println("A record was found in the DB!");
            
            uv.isvalidated = true;
            System.out.println("IsAdmin: " + document.getBoolean("isadmin"));
            uv.isadmin = document.getBoolean("isadmin");
            uv.code = "200";  
            uv.message = "A record was found in the DB!";
        }; 
        
        mongoClient.close();       
        
        return uv;
    }
    
    public UserValidation validateSandboxUser(String email, String password) throws Exception {
     
        String db_name = "users"; 
        UserValidation uv = new UserValidation();
             
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection("login_accounts_sandbox");

        Bson filter = Filters.and(
            Filters.eq("email", email),
            Filters.eq("password", password));
        Document document = collection.find(filter).first();
        
        if (document == null) { 
            System.out.println("Nothing was found in DB"); 
            uv.isvalidated = false;
            // Run query a second time to get details about missing data
            document = collection.find(eq("email", email)).first();
            if (document == null) {
                System.out.println("Email not was found in DB");
                uv.code = ""; 
                uv.message = "Email not was found in DB";
                return uv;
            } else { 
                System.out.println("Email was found in the DB, but password is wrong!");                
                uv.code = ""; 
                uv.message = "Email was found in the DB, but password is wrong!";
                return uv;
            }             
        } else { 
            System.out.println("A record was found in the DB!");
            
            uv.isvalidated = true;
            System.out.println("IsAdmin: " + document.getBoolean("isadmin"));
            uv.isadmin = document.getBoolean("isadmin");
            uv.code = "200";  
            uv.message = "A record was found in the DB!";
        }; 
        
        mongoClient.close();       
        
        return uv;
    }
    
    public String registerUser(String email, String password, String screenname /* can be omitted */) throws Exception {        
        
        String db_collection = "login_accounts"; 
        String result = null;
        boolean newBTCaddress_result = false;
        boolean newETHaddress_result = false;
        boolean newLTCaddress_result = false;
        boolean newWAVESaddress_result = false;
        boolean newRUBwallet_result = false;
        boolean newUSDwallet_result = false;
        boolean newHKDwallet_result = false;
        String newBTCaddress = null;
        String newLTCaddress = null;
        String newETHaddress = null;
        String newWAVESaddress = null;
        
        System.out.println("RegisterUser> screenname: " + screenname + ", Screenname length: " + screenname.length());  
        
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(db_collection);
        
        Document document = collection.find(eq("email", email)).first();
        
        if (document == null) {
            // Generate new BTC address and insert into DB (inserted inside of GetNewAddress function
            Bitcoin_Blockcypher bitcoin = new Bitcoin_Blockcypher();
            JSONObject result_obj  = bitcoin.getNewAddress(email, "test");
            if (result_obj.get("status").equals("-1")) {
                System.out.println("RegisterUser> failure gettings BTC address: " + result_obj.get("data").toString());
            } else {
                newBTCaddress = result_obj.get("data").toString();
                System.out.println("RegisterUser> new BTC address: " + newBTCaddress);
                newBTCaddress_result = true;
            }            
            
            // Generate new LTC address and insert into DB (inserted inside of GetNewAddress function
            Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher();
            result_obj  = litecoin.getNewAddress(email, "test");
            if (result_obj.get("status").equals("-1")) {
                System.out.println("RegisterUser> failure gettings LTC address: " + result_obj.get("data").toString());
            } else {
                newLTCaddress = result_obj.get("data").toString();
                System.out.println("RegisterUser> new LTC address: " + newLTCaddress);
                newLTCaddress_result = true;
            }          
            
            // Generate new ETH address and insert into DB (inserted inside of GetNewAddress function
            Ethereum_Blockcypher eth = new Ethereum_Blockcypher();
            result_obj = eth.getNewAddress(email, "test");
            if (result_obj.get("status").equals("-1")) {
                System.out.println("RegisterUser> failure gettings ETH address: " + result_obj.get("data").toString());
            } else {
                newETHaddress = result_obj.get("data").toString();
                System.out.println("RegisterUser> new ETH address: " + newBTCaddress);
                newETHaddress_result = true;
            }
            
            // Generate new WAVES address and insert into DB (inserted inside of GetNewAddress function
            Waves waves = new  Waves();
            result_obj  = waves.getNewAddress(email, "test");
            if (result_obj.get("status").equals("-1")) {
                System.out.println("RegisterUser> failure gettings WAVES address: " + result_obj.get("data").toString());
            } else {
                newWAVESaddress = result_obj.get("data").toString();
                System.out.println("RegisterUser> new WAVES address: " + newWAVESaddress);
                newWAVESaddress_result = true;
            }
            
            // Generate new RUB wallet 
            if (createNewFiatWallet(email, "RUB")) {            
                System.out.println("RegisterUser> a RUB wallet is created!");
                newRUBwallet_result = true;
            } else {
                System.out.println("RegisterUser> failure when cresating a RUB wallet");
                newRUBwallet_result = false;
            }
            
            // Generate new USD wallet 
            if (createNewFiatWallet(email, "USD")) {            
                System.out.println("RegisterUser> a USD wallet is created!");
                newUSDwallet_result = true;
            } else {
                System.out.println("RegisterUser> failure when cresating a USD wallet");
                newUSDwallet_result = false;
            }
            
            // Generate new HKD wallet 
            if (createNewFiatWallet(email, "HKD")) {            
                System.out.println("RegisterUser> a HKD wallet is created!");
                newHKDwallet_result = true;
            } else {
                System.out.println("RegisterUser> failure when cresating a HKD wallet");
                newHKDwallet_result = false;
            }
            
            // BTC, LTC, ETH and WAVES wallet data are already in DB (inserted in new address functions)
            if (newBTCaddress_result && 
                newETHaddress_result && 
                newLTCaddress_result && 
                newWAVESaddress_result &&
                newRUBwallet_result &&
                newUSDwallet_result &&
                newHKDwallet_result) {

                document = new Document();
                document.put("screen_name", screenname);
                document.put("email", email);
                document.put("password", password);
                document.put("account_status", null);
                document.put("date_created", new Date());
                document.put("date_approved", null);
                document.put("isadmin", false);                
                collection.insertOne(document);      

                // Insert generated user assets into balances_sandbox
                collection = db.getCollection("balances");
                document = new Document();
                document.put("user", email);
                List<Document> docs = new ArrayList<Document>();
                Document doc1 = new Document();
                doc1.put("name", "BTC");
                doc1.put("address", newBTCaddress);
                docs.add(doc1);
                Document doc2 = new Document();
                doc2.put("name", "LTC");
                doc2.put("address", newLTCaddress);
                docs.add(doc2);
                Document doc3 = new Document();
                doc3.put("name", "ETH");
                doc3.put("address", newETHaddress);
                docs.add(doc3);
                Document doc4 = new Document();
                doc4.put("name", "WAVES");
                doc4.put("address", newWAVESaddress);
                docs.add(doc4);
                Document doc5 = new Document();
                doc5.put("name", "RUB");
                doc5.put("address", "Fintech Gateway");
                docs.add(doc5);
                Document doc6 = new Document();
                doc6.put("name", "USD");
                doc6.put("address", "Fintech Gateway");
                docs.add(doc6);
                Document doc7 = new Document();
                doc7.put("name", "HKD");
                doc7.put("address", "Fintech Gateway");
                docs.add(doc7);
                document.put("assets", docs);                
                
                collection.insertOne(document);              
                result = "200"; 
            } else {
                result = "Failure while generating address!";   
            }            
                       
        } else {
            result = "The user already exists!"; 
        }     
        
        mongoClient.close(); 
        return result;
    }
    
    public String registerSandboxUser(String email, String password, String screenname /* can be omitted */) throws Exception {        
        
        String db_collection = "login_accounts_sandbox"; 
        String result = null;
        boolean newaddress1_result = false;
        boolean newaddress2_result = false;
        String newETHaddress = null;
        String newWAVESaddress = null;
        
        System.out.println("RegisterUser> screenname: " + screenname + ", Screenname length: " + screenname.length());  
        
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(db_collection);
        
        Document document = collection.find(eq("email", email)).first();
        
        if (document == null) {
             // Generate new ETH address and insert into DB (inserted inside of GetNewAddress function)
            EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
            newETHaddress = eth.getNewAddress(email, password);
            System.out.println("RegisterUser>new  ETH address: " + newETHaddress);
            if (newETHaddress.indexOf("0x") == -1) {                
                System.out.println("RegisterUser> failure gettings ETH address: " + newETHaddress);
            } else {
                newaddress1_result = true;
            }
           
            // Generate new WAVES address and insert into DB (inserted inside of getNewAddress function)
            WavesTestnet waves = new  WavesTestnet();
            String resultWAVES = waves.getNewAddress(email, "test");
            if (resultWAVES.indexOf("address") == -1) {  
                System.out.println("RegisterUser> failure gettings WAVES address: " + resultWAVES);                
            } else {
                JSONObject obj = new JSONObject(resultWAVES);
                newWAVESaddress = obj.getString("address");
                System.out.println("RegisterUser> new waves address: " + newWAVESaddress); 
                newaddress2_result = true;
            }           
            
            // ETH and WAVES wallet data are already in DB (inserted in new address functions)
            if (newaddress1_result && newaddress2_result) {
                document = new Document();
                document.put("screen_name", screenname);
                document.put("email", email);
                document.put("password", password);
                document.put("account_status", null);
                document.put("date_created", new Date());
                document.put("date_approved", null);
                document.put("isadmin", false);                
                collection.insertOne(document);      

                // Insert generated user assets into balances_sandbox
                collection = db.getCollection("balances_sandbox");
                document = new Document();
                document.put("user", email);
                List<Document> docs = new ArrayList<Document>();
                Document doc1 = new Document();
                doc1.put("name", "ETH");
                doc1.put("address", newETHaddress);
                docs.add(doc1);
                Document doc2 = new Document();
                doc2.put("name", "WAVES");
                doc2.put("address", newWAVESaddress);
                docs.add(doc2);
                document.put("assets", docs);                
                
                collection.insertOne(document);               
                
                mongoClient.close();
                
                result = "200"; 
            } else {
                result = "Failure while generating address!";   
            }            
                       
        } else {
            result = "The user already exists!"; 
        }     
        
        return result;
    } 
    
    public boolean createNewFiatWallet(String email, String currency)  throws Exception { 
        
        double balance = 0.0;        
        String db_name = "wallets";
        String db_collection = "";
        Settings settings = new Settings();
        boolean result = false;
        
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
        
        JSONObject results_obj = new JSONObject();
        String date = new Date().toString();
        results_obj.put("date_created", date);        
        results_obj.put("email", email); 
        results_obj.put("balance", balance);
        
        // Insert TX results into MongoDB        
        try {
            MongoCredential credential = MongoCredential.createCredential(
                    settings.user, 
                    db_name, 
                    settings.password.toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(
                    Settings.mongodb_host, 
                    settings.mongodb_port),
                    Arrays.asList(credential));
            
            Document doc = Document.parse(results_obj.toString());
            mongoClient.getDatabase(db_name).getCollection(db_collection).insertOne(doc);
            mongoClient.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}

