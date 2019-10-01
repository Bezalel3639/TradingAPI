package com.bezalel.trading_api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.bson.Document;
import org.codehaus.jettison.json.JSONObject; // jettison keeps order in JSON 

import com.mongodb.MongoClient;

public class Utils {
    
    public String LogTXResults (
            String pair, 
            double amount,
            boolean fixed_base_amount, 
            String address_base_from,
            String address_base_to,
            String address_quote_from,
            String address_quote_to,
            String base_exchangetx_results,
            String quote_exchangetx_results) throws Exception { 
        
        JSONObject results_obj = new JSONObject();
        String date = new Date().toString();
        results_obj.put("date", date);
        
        results_obj.put("pair", pair); // ETH_WAVES_BUY
        results_obj.put("amount", amount);
        results_obj.put("fixed_base_amount", fixed_base_amount);
        results_obj.put("address_base_from", address_base_from);
        results_obj.put("address_base_to", address_base_to);
        results_obj.put("address_quote_from", address_quote_from);
        results_obj.put("address_quote_to", address_quote_to);
            
        JSONObject txresults_obj = new JSONObject();
        txresults_obj.put("1st TX", quote_exchangetx_results);
        txresults_obj.put("2nd TX", base_exchangetx_results);

        results_obj.put("TX results", txresults_obj);  
        
        // Write results file
        Settings settings = new Settings();
        String txresults_file = null;
        String OS = System.getProperty("os.name");
        if (OS.equals("Windows 10")) {
            // Add TX results to log file
            txresults_file = settings.win10_store + "\\txresults_file.txt";            
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(new File(txresults_file), true));
                writer.write(results_obj.toString() + "\n");
            } catch (Exception e) {        
                e.printStackTrace();
            }           
            writer.close();    
        }
        else if (OS.equals("Linux")) {
             // Insert TX results into MongoDB             
            try {
                MongoClient mongoClient = new MongoClient(settings.mongodb_host, settings.mongodb_port);     
                Document doc = Document.parse(results_obj.toString());
                mongoClient.getDatabase("logs").getCollection("sandbox_transactions").insertOne(doc);
                mongoClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  
     
        return results_obj.toString();
    } 
}
