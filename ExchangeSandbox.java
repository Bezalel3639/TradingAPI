package com.bezalel.trading_api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class ExchangeSandbox {
    
    public static double getPairQuote(
            String asset1, 
            double asset1_amount, 
            String asset2, 
            String side) {

        Settings settings = new Settings();
        String uri = settings.uri;
        double asset1_price = 0;
        double asset2_price = 0;
        double assets_rate = 0;	    
   
        String asset1ID = getAssetId(asset1);
        String asset2ID = getAssetId(asset2);
        String arg = asset1ID + "," + asset2ID;
        
        if (!side.toUpperCase().equals("BUY") && !side.toUpperCase().equals("SELL")) {
            System.out.println("Side: 403"); 
            return -1; // return validation error
        }
        if (asset1.toUpperCase() == asset2.toUpperCase()) {
            System.out.println("403"); // forbidden
            return -1; // return validation error
        }
        if (asset1ID == "404" || asset2ID == "404") {
            System.out.println("404"); // not found
            return -1; // return validation error
        }
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("id", arg)); // <crypto1_id>,<crypto2_id> 
    
        try {
            Waves waves = new  Waves(); // no relation to Waves API
            String result = waves.makeAPICall(uri, parameters);
               JSONObject obj = new JSONObject(result);
            JSONObject objnext = obj.getJSONObject("data");
            System.out.println(obj.toString()); 
            JSONObject obj_1 = objnext.getJSONObject(asset1ID);
            JSONObject obj_2 = objnext.getJSONObject(asset2ID);
            
            objnext = obj_1;
            objnext = objnext.getJSONObject("quote");
            objnext = objnext.getJSONObject("USD");
            asset1_price = objnext.getDouble("price");  
            System.out.println(asset1 + " price: " + asset1_price);
            
            objnext = obj_2;
            objnext = objnext.getJSONObject("quote");
            objnext = objnext.getJSONObject("USD");
            asset2_price = objnext.getDouble("price");  
            System.out.println(asset2 +" price: " + asset2_price);
            
            if (asset1_price == 0) {
                System.out.println("403"); // forbidden
                return -1; // return validation error (zero denominator)		    
            }
        
            assets_rate = asset2_price/asset1_price;
            System.out.println("1 " + asset2 + " is: " + assets_rate + " " + asset1); 
        } catch (IOException e) {
            System.out.println("Error: cannont access content - " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.toString());
        }
        
        String pair = asset1.toUpperCase() + "_" + asset2.toUpperCase();
        System.out.println("Pair: " + pair + ", Side: " + side.toUpperCase() + 
                             ", Fee: " + getFeeValue(pair, side.toUpperCase()));	    
    
        if (assets_rate == 0) {	    	
            return -1; // return validation error (zero denominator)  
        }

        // Fee value in percents
        double quote = (1+getFeeValue(pair, side.toUpperCase()))*asset1_amount/assets_rate;
        System.out.println("Quote: " + quote);
        
        return quote;
    }
        
    public static String getAssetId(String asset) {
        
        String ASSET_NOT_FOUND = "404";
        
        Settings settings = new Settings();
        settings.CollectionCryptoIDs(); // TODO: move to WAR initializer
        if (settings.cryptoids.get(asset.toUpperCase()) == null) {
            return ASSET_NOT_FOUND;  
        }
        
        System.out.println(settings.cryptoids.get(asset.toUpperCase()));
        
        return settings.cryptoids.get(asset.toUpperCase());
    }
     
    public static double getFeeValue(String pair, String side) {
        
         // Format sample: BTC_WAVES_SELL
          String value = pair.toUpperCase() + "_" + side.toUpperCase();
          
         Settings settings = new Settings();
         settings.CollectionCurrencyPairs(); // TODO: move to WAR initializer
        if (settings.pairs.get(value) == null) {
            System.out.println("The pair is not registered");  
        }
         
         return settings.pairs.get(value);
    }
    
    public String ExchangePair(
            String type, 
            String asset1, 
            double asset1_amount,
            String asset1_address, 
            String asset2,
            double asset2_amount,
            String asset2_address) throws Exception {
        
         String result = null;
         
         // Scenario 1: Buy X BTC for a set amount (asset2_amount = 0.00233) of Waves. The pair 
         // is BTC_WAVES_SELL.  
        if (!asset1.toUpperCase().equals("BTC") && !asset1.toUpperCase().equals("WAVES")) {
            System.out.println("Side: 403"); 
            // Return validation error (only BTC and WAVES TX sandbox TX are implemented)
            return "-1"; 
        }
         
         String asset1_reservesaddress = null;
         String asset2_reservesaddress = null;
        
        Settings settings = new Settings();
        settings.CollectionCryptoIDs();
        settings.CollectionSymbols();
        System.out.println(settings.cryptoids.get("Waves")); //1274 
        System.out.println(settings.symbols.get(1274)); // Waves
         
        // Step 1: Get quote
        double quote = getPairQuote(asset1, 1, asset2, type);
        System.out.println("This quote: " + quote); 
        
        // Step 2: Get reserves addresses for the selected pair (BTC_WAVES).
        settings.CollectionKeys();
        JSONObject obj = new JSONObject(settings.reserves_addresses.toString()); 
        asset1_reservesaddress = obj.get(asset1.toUpperCase()).toString();
        asset2_reservesaddress = obj.get(asset2.toUpperCase()).toString();		
        System.out.println(asset1 + " reserves address: " + asset1_reservesaddress); 
        System.out.println(asset2 + " reserves address: " + asset2_reservesaddress);     	
        
         // Step 3: Withdraw from the user account the set amount of asset2 (Waves) to 
        // asset2_reservesaddress of Waves reserves address before sending to 
        // the user asset1 (BTC)
        
        // Step 3A: Get Waves seed for the user Waves account      
         obj = new JSONObject(settings.wavesobj.toString());
          System.out.println(obj.get(asset2_address)); 
         String sourceseed = obj.get(asset2_address).toString(); 
         
        // Step 3B: Check if Waves balance is enough and exceeds minimum TX value (TODO)
         WavesTestnet waves = new  WavesTestnet();
         long userbalance = waves.getBalance(asset2_address);
         System.out.println("Waves user balance: " + String.valueOf(userbalance));
         if (userbalance < asset2_amount*100000000) { 	    	
             return "Insufficient user balance";
         }
         
         // Step 3C: Withdraw Waves from user address	    
        long amount = new Double(asset2_amount*100000000).longValue(); 
        String message = "Test TX";
        String res = waves.SendTX(sourceseed, asset2_reservesaddress, amount, message);        
        
        // Step 4: Check if the result is success, log TX id, and if not return a message (TODO).
        System.out.println(res); 
        if (res.length() != 44)
        {
            System.out.println(res.length()); 
            return "Withdrawal transaction failed";
        }
        
        // Step 5: Send the user the quoted amount of BTC from reserves 
        
        // Step 5A: Get the user BTC address 
  
        // Step 5B: Convert quote to satoshi
        long amount2 = new Double(asset2_amount*100000000/quote).longValue(); // 26 (TODO: not all digits)
        System.out.println("Quote: " + quote + ", Amount:" + amount2); 
        
        // Step 5C: Check if BTC balance is enough and exceeds minimum TX value 
        // (TODO and TODO: should be before all TX)
        
        // Note: this step involves consideration of how even small amount of BTC are 
        // sent from A to B on BTC testnet. If 1368 BTC are send from A to B, the funds are 
        // sent to B and A, the same source. 611574=1368+610206 from A as 1368 to B and 
        // 610206 back to A. A fee of 13600 from withdrawn from A and addition to 1368 
        // as send amount.
        
        BitcoinTestnet_Blockcypher bitcoin = new BitcoinTestnet_Blockcypher();
        long reserves_balance = bitcoin.getBalance(asset1_reservesaddress);
        System.out.println("The BTC sandbox reserves balance: " + reserves_balance);
        
        // Step 5D: Get privatekey & publickey of reserves BTC address
         settings.CollectionKeys(); // TODO: move to WAR initializer
         obj = new JSONObject(settings.keysobj.toString());
        JSONObject child = obj.getJSONObject("BTC");
        System.out.println(child.toString()); 
        child = child.getJSONObject(asset1_reservesaddress);
        System.out.println("Private key: " + child.get("Private key").toString()); 
        System.out.println("Public key: " + child.get("Public key").toString());
        String privatekey = child.get("Private key").toString();
        String publickey = child.get("Public key").toString(); 	
        
        bitcoin = new BitcoinTestnet_Blockcypher();
        result = bitcoin.SendBTC(asset1_reservesaddress, asset1_address, 
                                String.valueOf(amount2), privatekey, publickey); 
        System.out.println(result); 
        
        // Step 6: Check if the result is success, log TX id, and if not rollback withdrawal
        // Waves from the user account, error message. TODO
        
        return result;
    }
    
    public String getFeeStructure() {   
        
        Settings settings = new Settings();
         settings.CollectionCurrencyPairs(); // TODO: move to WAR initializer
         
        Map<String, Object> data = new HashMap<String, Object>(); 	
          for (Map.Entry<String, Double> entry : settings.pairs.entrySet()) {
             data.put(entry.getKey(), entry.getValue());
         }

        JSONObject json = new JSONObject(data);		
        System.out.println(json.toString());

        String result = json.toString();
        
        return result; 
    }
}
