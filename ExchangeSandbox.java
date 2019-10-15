package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bson.Document;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Int;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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
	    double assets_rate_new = 0;    
   
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
		    System.out.println("GetPairQuote> " + asset1 + " price: " + asset1_price + " USD");
		    
		    objnext = obj_2;
		    objnext = objnext.getJSONObject("quote");
		    objnext = objnext.getJSONObject("USD");
		    asset2_price = objnext.getDouble("price");  
		    System.out.println("GetPairQuote> " + asset2 +" price: " + asset2_price + " USD");
		    
		    if (asset1_price == 0) {
		    	System.out.println("403"); // forbidden
		    	return -1; // return validation error (zero denominator)		    
		    }
	    
		    assets_rate = asset2_price/asset1_price;
		    assets_rate_new = asset1_price/asset2_price;
	    } catch (IOException e) {
	        System.out.println("GetPairQuote> Error: cannont access content - " + e.toString());
	    } catch (URISyntaxException e) {
	        System.out.println("GetPairQuote> Error: Invalid URL " + e.toString());
	    }
	    
	    String pair = asset1.toUpperCase() + "_" + asset2.toUpperCase();
	    System.out.println("GetPairQuote> Pair: " + pair + ", Side: " + side.toUpperCase() + 
	    		             ", Fee: " + getFeeValue(pair, side.toUpperCase()));
        double exchange_rate_buy = (1 + getFeeValue(pair, "BUY"))*asset1_price/asset2_price;
        double exchange_rate_sell = (1 + getFeeValue(pair, "SELL"))*asset1_price/asset2_price;
  
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("GetPairQuote> 1 " + asset1 + " market rate: " + assets_rate_new + " " + asset2);
        System.out.println("GetPairQuote> 1 " + asset1 + " exchange rate (BUY): " + exchange_rate_buy + " " + asset2); 
        System.out.println("GetPairQuote> 1 " + asset1 + " exchange rate (SELL): " + exchange_rate_sell + " " + asset2); 
        System.out.println("--------------------------------------------------------------------------------");

        
	    if (assets_rate == 0) {	    	
	    	return -1; // return validation error (zero denominator)  
	    }

	    // Fee value in percents
	    double quote = (1 + getFeeValue(pair, side.toUpperCase()))*asset1_amount*assets_rate_new;
	    System.out.println("GetPairQuote> Quote: " + quote);
	    System.out.println("GetPairQuote> You'll need " + quote + " WAVES for " + asset1_amount + " ETH");
	    
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
   
    public String ExchangePair_ETH2WAVES(
              String type,               
              double amount,                           
              String asset_base_address, 
              String asset_quoted_address, 
              boolean fixed_base_amount) throws Exception {
 
        String result = null;
        String asset_base = "ETH"; 
        String asset_quoted  = "WAVES"; 
        
        if (!type.toUpperCase().equals("BUY") && !type.toUpperCase().equals("SELL")) {
            System.out.println("Side: 403"); 
            // Return validation error ("Buy" or "Sell" are the only allowed values)
            return "-1"; 
        }
        
        String asset_base_reservesaddress = null;
        String asset_quoted_reservesaddress = null;
        double quote = 0;
        
        Settings settings = new Settings();
        settings.CollectionCryptoIDs();
        settings.CollectionSymbols();
        System.out.println(settings.cryptoids.get("Waves"));  
        System.out.println(settings.symbols.get(1274));
        
        // Get reserves addresses for the selected pair (ETH_WAVES). Common step for all scenarios.
        settings.CollectionKeys();
        JSONObject obj = new JSONObject(settings.reserves_addresses.toString()); 
        asset_base_reservesaddress = obj.get(asset_base.toUpperCase()).toString();
        asset_quoted_reservesaddress = obj.get(asset_quoted.toUpperCase()).toString(); 

        System.out.println(asset_base + " reserves address: " + asset_base_reservesaddress); 
        System.out.println(asset_quoted + " reserves address: " + asset_quoted_reservesaddress); 
        
        String first_tx_res = null;
        String second_tx_res = null;
                
        switch (type.toUpperCase()) {
            case "SELL":            
                // Get quote                
                if (!fixed_base_amount) { // X ETH for a set amount of Waves
                   quote = getPairQuote(asset_base, 1, asset_quoted, type);
                }
                else if (fixed_base_amount) {
                // Get amount of Waves required to exchange for fixed amount of base currency (ETH).
                    quote = getPairQuote(asset_base, amount, asset_quoted, type);
                }
                else  {
                    return "The scenario does not exist";
                }
                    
                System.out.println("ExchangePair_ETH2WAVES> This quote: " + quote); 
                
                if (!fixed_base_amount) {
                    first_tx_res = SendWaves2Waves(amount, asset_quoted_reservesaddress, asset_quoted_address);
                }
                else if (fixed_base_amount) {
                    first_tx_res = SendWaves2Waves(quote, asset_quoted_reservesaddress, asset_quoted_address);
                }  else  {
                    return "The scenario does not exist";
                }
                  
                double amount_eth = 0;                  
                if (!fixed_base_amount) {
                    amount_eth = amount/quote;
                    System.out.println(amount_eth + "is ETH amount to be received for " + amount + " WAVES");
                }
                else if (fixed_base_amount) {   
                    amount_eth = amount;
                }                    
                    
                second_tx_res =   SendETH2ETH(amount_eth, asset_base_reservesaddress, asset_base_address);
                
                break;
            case "BUY":                
                  // Get quote
                  amount_eth = 0;
                  if (fixed_base_amount) {
                      quote = getPairQuote(asset_base, amount, asset_quoted, type);
                      System.out.println("Quote here: " + quote);
                  }
                  else if (!fixed_base_amount) { 
                      quote = getPairQuote(asset_base, 1, asset_quoted, type); //1 ETH = XXX WAVES
                      System.out.println("1 ETH is " + quote + " WAVES");
                      amount_eth = amount/quote;
                  }

                  double amount_log = 0;
                  if (fixed_base_amount) {                      
                      first_tx_res = SendETH2ETH(amount /* ETH */, asset_base_address, asset_base_reservesaddress);
                      amount_log = amount;
                      System.out.println("ETH amount: " + amount + " ETH");
                  } else {
                      BigDecimal bd = BigDecimal.valueOf(amount_eth).setScale(10, BigDecimal.ROUND_HALF_UP);
                      System.out.println("ETH amount (original): " + amount_eth + "ETH");
                      amount_log = bd.doubleValue();                      
                      first_tx_res = SendETH2ETH(bd.doubleValue() /* ETH */, asset_base_address, asset_base_reservesaddress); 
                  }                  
                  System.out.println("--------------------------------------------------------------------------------");
                  System.out.println("ETH amount: " + amount_log + " ETH");
                  System.out.println("ETH address (from): " + asset_base_address);
                  System.out.println("ETH address (to): " + asset_base_reservesaddress);
                  System.out.println("--------------------------------------------------------------------------------");
                  
                  // TODO: check error (if the result of 1st tx): abort
                  
                  if (fixed_base_amount) {
                      second_tx_res = SendWaves2Waves(quote /* WAVES */, asset_quoted_reservesaddress, asset_quoted_address);
                      amount_log = quote;
                  } else {
                      second_tx_res = SendWaves2Waves(amount /* WAVES */, asset_quoted_reservesaddress, asset_quoted_address);
                       amount_log = amount;
                  }
                  System.out.println("--------------------------------------------------------------------------------");
                  System.out.println("WAVES amount (quote): " + amount_log);
                  System.out.println("WAVES address (from): " + asset_quoted_reservesaddress);
                  System.out.println("WAVES address (to): " + asset_quoted_address);
                  System.out.println("--------------------------------------------------------------------------------");
                  
                  // TODO: check error (if the result of 2ndt tx): abort + return ETH
                  
                break;
            default:
                System.out.println("ExchangePair> Invalid type of trasaction");           
        }
        
        // Log transaction results
        Utils log = new  Utils();
        
        String pair = "ETH_WAVES_"+type.toUpperCase();       
        String address_base_from = null;
        String address_base_to = null;
        String address_quote_from = null;
        String address_quote_to = null;
        String base_exchangetx_results = null;
        String quote_exchangetx_results = null;
        
        if (type.toUpperCase().equals("SELL")) {
            address_base_from = asset_base_reservesaddress;   // if fixed base, the reserves address
            address_base_to = asset_base_address; 
            address_quote_from = asset_quoted_address;
            address_quote_to = asset_quoted_reservesaddress;
            base_exchangetx_results = second_tx_res;
            quote_exchangetx_results = first_tx_res;
        } else if (type.toUpperCase().equals("BUY")) {
            address_base_from = asset_base_address;            // reversed relative to "SELL"
            address_base_to = asset_base_reservesaddress;      // reversed relative to "SELL"
            address_quote_from = asset_quoted_reservesaddress; // reversed relative to "SELL"
            address_quote_to = asset_quoted_address;           // reversed relative to "SELL"
            base_exchangetx_results = first_tx_res;            // reversed relative to "SELL"
            quote_exchangetx_results = second_tx_res;          // reversed relative to "SELL"
        }
            
        result = log.LogTXResults(
                pair, 
                amount, 
                fixed_base_amount, 
                address_base_from, 
                address_base_to, 
                address_quote_from, 
                address_quote_to, 
                base_exchangetx_results, 
                quote_exchangetx_results);
                
        return result;
    }   

    
    public static String SendWaves2Waves(
              double amount, 
              String address_from, 
              String address_to) throws Exception {
        
        Settings settings = new Settings();
        settings.CollectionKeys();        
      
        // Get Waves seed      
        JSONObject obj = new JSONObject(settings.wavesobj.toString());
        System.out.println(obj.get(address_from)); 
        String sourceseed = obj.get(address_from).toString(); 
      
        // TODO: check if Waves balance is enough and exceeds minimum TX value (TODO)
        WavesTestnet waves = new  WavesTestnet();
        long userbalance = waves.getBalance(address_from);
        System.out.println("Waves user balance: " + String.valueOf(userbalance));
      
        // Withdraw Waves 
        long amount_baseunits = 0;
        amount_baseunits = new Double(amount*100000000).longValue();
        System.out.println("The amount of Waves (units) to be withdrawn: " + amount_baseunits);
      
        String message = "Test TX";
        String res = waves.SendTX(sourceseed, address_to, amount_baseunits, message);
        System.out.println("SendWaves2Waves, TX result: " + res);       
        
        return res;
    }
    
    public String SendBTC2BTC(
            long amount, 
            String asset1_reservesaddress, 
            String asset1_address) throws Exception {
        
        String result = null;
        Settings settings = new Settings();
        
        // TODO: check if BTC balance is enough and exceeds minimum TX value 
        
        // Note: This step involves consideration of how even small amount of BTC is 
        // sent from A to B on BTC testnet. If 1368 units is send from A to B, the funds are 
        // sent to B and A, the same source. 611574=1368+610206 from A as 1368 to B and 
        // 610206 back to A. A fee of 13600 is withdrawn from A and addition to 1368 
        // as send amount.
      
        BitcoinTestnet_Blockcypher bitcoin = new BitcoinTestnet_Blockcypher();
        long reserves_balance = bitcoin.getBalance(asset1_reservesaddress);
        System.out.println("SendBTC2BTC> The BTC sandbox reserves balance: " + reserves_balance);
      
        // Get privatekey & publickey of reserves 
        settings.CollectionKeys(); // TODO: move to WAR initializer
        JSONObject obj = new JSONObject(settings.keysobj.toString());
        JSONObject child = obj.getJSONObject("BTC");
        System.out.println("SendBTC2BTC>" + child.toString()); 
        child = child.getJSONObject(asset1_reservesaddress);
        System.out.println("Private key: " + child.get("Private key").toString()); 
        System.out.println("Public key: " + child.get("Public key").toString());
        String privatekey = child.get("Private key").toString();
        String publickey = child.get("Public key").toString();  
      
        result = bitcoin.SendBTC(asset1_reservesaddress, asset1_address, 
                              String.valueOf(amount), privatekey, publickey); 
        System.out.println("SendBTC2BTC, TX result: " + result); 
    
        return result;
    }
    
    public String SendETH2ETH(
            double amount, 
            String addressfrom, // from reserves if ETH_WAVES_BUY
            String addressto) throws Exception {
        
        Settings settings = new Settings();
        settings.CollectionKeys();        
       
        // Get Waves seed   
        JSONObject obj = settings.locateETHWalletByAddress(addressfrom);
        System.out.println(obj.get("wallet")); 
        String walletfrom = obj.getString("wallet"); 
        String password = obj.getString("password");
        System.out.println("Wallet from: " + walletfrom); 
        
        // TODO: check if ETH balance is enough and exceeds minimum TX value 
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();        
        BigInteger addressfrom_balance = eth.getBalance(addressfrom);
        System.out.println("ETH addressfrom balance: " + String.valueOf(addressfrom_balance)); // ETH addressfrom balance: 853171079733644000
      
        // Get full file name for the wallet and send ETH
        String walletfile = null;
        String OS = System.getProperty("os.name");
        if (OS.equals("Windows 10")) {
            walletfile = settings.win10_devfolder + "\\" + walletfrom;
        }
        else if (OS.equals("Linux")) {
            walletfile = settings.linux_store + "/" + walletfrom;
        }
        
        String res = eth.SendETH(walletfile, password, addressto, amount /* ETH */);        
        System.out.println("SendETH2ETH, TX result: " + res); 
         
        return res;
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
    
    public JSONObject readDB_ETHWalletByAddress(String address) throws Exception {
        
        Settings settings = new Settings();
        MongoClient mongoClient = new MongoClient(settings.mongodb_host, settings.mongodb_port);       
             
        MongoDatabase database = mongoClient.getDatabase("wallets");
        MongoCollection<Document> collection = database.getCollection("ETH (testnets)");

        Document document = collection.find(eq("address", address)).first();
        if (document == null) {             
            System.out.println("Nothing was found in DB");
            mongoClient.close(); 
            return null;
        }; 
         
        JSONObject obj = new JSONObject(document.toJson());    
        mongoClient.close(); 

        return obj;
    }   
}
