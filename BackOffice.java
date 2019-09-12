package com.bezalel.trading_api;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import static com.mongodb.client.model.Filters.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

public class BackOffice {
    
    Settings settings = new Settings();
    MongoClient mongoClient = new MongoClient(settings.mongodb_host, settings.mongodb_port);
    
    public String GetUserAssets(String user) throws Exception {    
        MongoDatabase database = mongoClient.getDatabase("users");
        MongoCollection<Document> collection = database.getCollection("investors");

        Document document = collection.find(eq("user", user)).first();
        if (document == null) {             
            System.out.println("Nothing was found in DB");
            return "404"; // "Not found" HTTP code
        }; 
         
        JSONObject obj = new JSONObject(document.toJson());       
        JSONArray objnext = obj.getJSONArray("assets");
        System.out.println(objnext.toString());
        String data= objnext.toString();
        mongoClient.close();
  
        return data;
    }
    
    public String AddAsset(String user, String asset, String address) throws Exception {     
        MongoDatabase database = mongoClient.getDatabase("users");
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
    
    public String RemoveAsset(String user, String asset, String address) throws Exception {
        MongoDatabase database = mongoClient.getDatabase("users");
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
    
    public String UpdateAssetData(String user, int index, String newdata) throws Exception {      
        MongoDatabase database = mongoClient.getDatabase("users");
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
}

