package com.bezalel.trading_api;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.wavesplatform.wavesj.Account;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import org.bson.Document;
import org.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Waves {
    
    Settings settings = new Settings();

	public int getLastBlock() throws Exception {
		
		Node node = new Node(settings.url_waves_mainnet, Account.MAINNET);
		int wavesHeight = node.getHeight();
		System.out.println("GetLastBlock> Current WAVES height: " + node.getHeight()); 
	    	
		return wavesHeight;
	}

	public long getBalance(String address) throws Exception {
		
		Node node = new Node(settings.url_waves_mainnet, Account.MAINNET);
		long balance = node.getBalance(address);
		System.out.println("GetBalance> Balance: " + node.getBalance(address)); 
	
		return balance;
	}
	
	public double getWavesRate() {		

        double rate = 0;
        
	    List<NameValuePair> parameters = new ArrayList<NameValuePair>();
	    parameters.add(new BasicNameValuePair("id","1274")); 

	    try {
	        String result = makeAPICall(settings.url_rates, parameters);
	   	    JSONObject obj = new JSONObject(result);
    	    JSONObject objnext = obj.getJSONObject("data");
    	    objnext = objnext.getJSONObject("1274");
    	    objnext = objnext.getJSONObject("quote");
    	    objnext = objnext.getJSONObject("USD");
    	    rate = objnext.getDouble("price");    	    
 	    } catch (IOException e) {
	        System.out.println("GetWavesRate> Error: cannont access content - " + e.toString());
	    } catch (URISyntaxException e) {
	        System.out.println("GetWavesRate> Error: Invalid URL " + e.toString());
	    }
	    
	    return rate;
    }
	
    public JSONObject getNewAddress(String user_email, String notes) throws Exception {   

        String network = "Mainnet";
        String db_name = "wallets";
        String collection = "WAVES";
        
        // Validate DB connection 
        Settings settings = new Settings();
        Utils utils = new Utils();
        JSONObject result = null;
        System.out.println("GetNewAddress> Validation of DB connection...");
        JSONObject connect_result = new JSONObject();
        connect_result = utils.validateMongoDBConnection(settings.user, db_name, settings.password);  
        if (connect_result.get("status").equals("-1")) {          
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The operation rejected: " + connect_result.get("data").toString());
            return result;
        } else {
            System.out.println("GetNewAddress> " + connect_result.get("data").toString());  
        }
    
        Node node = new Node(settings.url_waves_mainnet, Account.MAINNET);  
        System.out.println("GetNewAddress> Waves node version: " + node.getVersion());
        
        // Generate seed
        String seed = PrivateKeyAccount.generateSeed();      
        
        // Generate address
        PrivateKeyAccount account = PrivateKeyAccount.fromSeed(seed, 0, Account.MAINNET); 
        byte[] publicKey = account.getPublicKey(); 
        String address = account.getAddress(); 
        
        System.out.println("GetNewAddress> Public Key: " + publicKey); 
        System.out.println("GetNewAddress> New WAVES mainnet address: " + address); 
        System.out.println("GetNewAddress> Balance: " + node.getBalance(address)); 
        
        // Prepare JSON wallet data for DB
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("address", address);
        data.put("publickey", publicKey.toString());
        data.put("privatekey", account.getPrivateKey().toString()); 
        data.put("balance", node.getBalance(address));
        JSONObject json = new JSONObject(data);        
        System.out.println("GetNewAddress> New WAVES wallet: " + json.toString());  
        String walletdata = json.toString();
        
        // Encrypt seed and wallet data
        Utils.TrippleDes td = utils.new TrippleDes();
        String seed_encrypted = td.encrypt(seed);
        String walletdata_encrypted = td.encrypt(walletdata);
        
        // Prepare JSON with all data for DB
        String wallet_json = utils.makeJSON_WavesWallet(address, seed_encrypted, network, user_email, walletdata_encrypted, notes);
        System.out.println("getNewAddress> WAVES wallet data: " + wallet_json);
        
        // Store new wallet into MongoDB             
        try {
            MongoCredential credential = MongoCredential.createCredential(
                    settings.user, 
                    db_name, 
                    settings.password.toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(
                    Settings.mongodb_host, 
                    settings.mongodb_port),
                    Arrays.asList(credential)); 
            
            Document doc = Document.parse(wallet_json);
            mongoClient.getDatabase(db_name).getCollection(collection).insertOne(doc);
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The operation rejected: failure to enter account data for address " + address + " into DB!");
            return result;
        }
        
        result = new JSONObject();
        result.put("status", "200");
        result.put("data", address);
        return result;
    }
	
	public String makeAPICall(String uri, List<NameValuePair> parameters)
		      throws URISyntaxException, IOException {
		
  	    String response_content = "";
	
	    URIBuilder query = new URIBuilder(uri);
	    query.addParameters(parameters);
	
	    CloseableHttpClient client = HttpClients.createDefault();
	    HttpGet request = new HttpGet(query.build());
	
	    request.setHeader(HttpHeaders.ACCEPT, "application/json");
	    request.addHeader("X-CMC_PRO_API_KEY", settings.rates_apikey);
	
	    CloseableHttpResponse response = client.execute(request);
	
	    try {
	        System.out.println(response.getStatusLine());
	        HttpEntity entity = response.getEntity();
	        response_content = EntityUtils.toString(entity);
	        EntityUtils.consume(entity);
	    } finally {
	        response.close();
	    }
	
	    return response_content;
    }
    	
	public boolean validateAddress(String address) throws Exception {
	        Node node = new Node(settings.url_waves_mainnet, Account.MAINNET);
	        return node.validateAddresses(address); 
	} 

    public JSONObject sendWAVES2WAVES(double amount /* WAVES */, String addressfrom, String addressto) throws Exception {
 
        String asset = "WAVES"; 
        String seed = "maple equal weekend gown forget shop dice mother season hat pitch mesh welcome error click"; 
        String destination = "3PDKC26V2sdxGtvnqmkgTwdwT6zeSB72UqK"; // Account #3 under wallets/WAVES
        long fee = 100_000; 
        long waves2baseunits = 100_000_000; // 1 WAVES = 100 000 000 units
        String message = "";
        JSONObject result = null; 
        Utils utils = new Utils();   
        
        // Validate DB connection 
        long startTimeCumulative = System.currentTimeMillis();
        System.out.println("SendWAVES2WAVES> Validation of DB connection...");
        String db_name = "wallets"; 
        JSONObject connect_result = new JSONObject();
        connect_result = utils.validateMongoDBConnection(settings.user, db_name, settings.password);        
        if (connect_result.get("status").equals("-1")) {          
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The transaction rejected: " + connect_result.get("data").toString());
            return result;
        } else {
            System.out.println("SendWAVES2WAVES> " + connect_result.get("data").toString());  
        }     
        
        // Validate addresses
        System.out.println("SendWAVES2WAVES> Validation of the source address...");
        if (!validateAddress(addressfrom)) {
            System.out.println("SendWAVES2WAVES> The source address " + addressfrom + " does not exist!");
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The source address " + addressfrom + " does not exist!");
            return result;
        } else {             
            System.out.println("SendWAVES2WAVES> The source address " + addressfrom + " is valid!");
        }       
        System.out.println("SendWAVES2WAVES> Validation of the destination address...");
        if (!validateAddress(addressto)) {
            System.out.println("SendWAVES2WAVES> The destination address does not exist");
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The destination address " + addressto + " does not exist!");
            return result;
        } else {             
            System.out.println("SendWAVES2WAVES> The destination address " + addressto + " is valid!");
        }
        
        // Validate if the transfer amount is inside min/max range
        JSONObject object = settings.getTransferRanges(asset);
        double min = object.getDouble("min");
        double max = object.getDouble("max");
        System.out.println("SendWAVES2WAVES> Validation of the transaction range...");
        System.out.println("SendWAVES2WAVES> The min/max transfer value is " + min + "/" + max + " WAVES");
        if ( amount >= min && amount <= max) {
            System.out.println("SendWAVES2WAVES> The transfer amount " + amount + " is inside the range!");
        } else if (amount < min) {
            System.out.println("SendWAVES2WAVES> The transaction rejected! The minimumun amount of transaction must be more or equal " + min + " WAVES!");  
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the minimumun amount of transaction must be more or equal " + min + " WAVES!");
            return result;
        } else if (amount > max) {
            System.out.println("SendWAVES2WAVES> The transaction rejected: the maximum amount of transaction must be less or equal " + max + " WAVES!");  
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the maximum amount of transaction must be less or equal " + max + " WAVES!");
            return result;
        }
        
        // Validate source balance 
        System.out.println("SendWAVES2WAVES> Validation of the source balance...");
        long userbalance = getBalance(addressfrom);
        long estimated_withdrawal_amount = (long)(amount /* WAVES */ * waves2baseunits) + fee;
        if (estimated_withdrawal_amount /* + plus Fintech Gateway transfer fee */ > userbalance) {
            System.out.println("SendWAVES2WAVES> Estimated amount of transfer including fee: " + estimated_withdrawal_amount + ", Source amount: " + userbalance);
            System.out.println("SendWAVES2WAVES> The transaction rejected: insufficient funds!");
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: insufficient funds! The source amount: " + userbalance + ", the estimated amount of transfer including fee: " + estimated_withdrawal_amount);
            return result;
        } else {
            System.out.println("SendWAVES2WAVES> The source balance " + userbalance + " is valid for transaction!");  
        }
        
        // Validate availability of wallet corresponding to the source address in MongoDB
        System.out.println("SendWAVES2WAVES> Validation of the source wallet availability in MongoDB...");
        JSONObject obj = readDB_WAVESWalletByAddress(addressfrom); 
        if (obj == null) {            
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the wallet was not found in the DB");
            return result;            
        } else {
            System.out.println("SendWAVES2WAVES> The source wallet is found in MongoDB!");
            System.out.println("SendWAVES2WAVES> The encrypted source seed: " + obj.get("seed")); 
            String encrypted_sourceseed = obj.get("seed").toString();
            Utils.TrippleDes td = utils.new TrippleDes();
            String sourceseed = td.decrypt(encrypted_sourceseed);
        }
                
        // All validations successfully completed
        System.out.println("SendWAVES2WAVES> The transfer is approved! The estimated amount of transfer including fee: " + estimated_withdrawal_amount);
     
        // 1 WAVES = 100 000 000 units, the minimum fee is 0.001 WAVES (or 100 000 units)
        PrivateKeyAccount account = PrivateKeyAccount.fromSeed(seed, 0, Account.MAINNET); 
        Node node = new Node(settings.url_waves_mainnet, Account.MAINNET);
        long amount_baseunits = new Double(amount*100000000).longValue();
        long startTime = System.currentTimeMillis();
        String tx_id = null;
        try {
            tx_id = node.transfer(account, destination, amount_baseunits, fee, message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            result = new JSONObject(); 
            result.put("status", "-1");
            result.put("data", "An error while executing transaction with node.transfer!");
            return result; 
        }
        long stopTime = System.currentTimeMillis();
        long startStopCumulative = stopTime;
        long elapsedTime = stopTime - startTime;
        long elapsedTimeCumulative = startStopCumulative - startTimeCumulative;
        System.out.println("SendWAVES2WAVES> The transfer is successfully completed!");
        System.out.println("SendWAVES2WAVES> TX result: " + tx_id);  
        Thread.sleep(3000); 
        String tx_data = null;
        try {
            tx_data = node.getTransactionData(tx_id).toString();
        } catch (IOException e) { 
            result = new JSONObject(); 
            result.put("status", "-1");
            result.put("data", "An error while getting transaction data for tx: " + tx_id + "The transaction was not add to the DB!");
            return result; 
        }
        System.out.println("SendWAVES2WAVES> The transaction data: " + tx_data);
        System.out.println("SendWAVES2WAVES> Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);
        System.out.println("SendWAVES2WAVES> Cutulative time (millisec): " + elapsedTimeCumulative + ", Time (sec): " + ((double) elapsedTimeCumulative)/1000 + ", Time (min): " + ((double) elapsedTimeCumulative)/1000/60);
          
        // Log transaction results
        int execution_time = (int) (((double) elapsedTime)/1000); 
        int cumulative_execution_time = (int) (((double) elapsedTimeCumulative)/1000); 
        String log = utils.logTransferTXResults("WAVES2WAVES", amount, addressfrom, addressto, tx_data, cumulative_execution_time);
        System.out.println("SendWAVES2WAVES> The transaction log added to MongoDB: " + log);
        
        result = new JSONObject(); 
        result.put("status", "200");
        result.put("data", "The transaction successfully executed for " + cumulative_execution_time + " sec!");
        return result; 
    }
    
    public JSONObject readDB_WAVESWalletByAddress(String address) throws Exception {
        String db_name = "wallets";
        String db_collection = "WAVES";        
        Settings settings = new Settings();        
        
        MongoCredential credential = MongoCredential.createCredential(
                settings.user, 
                db_name, 
                settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(
                settings.mongodb_host, 
                settings.mongodb_port),
                Arrays.asList(credential)); 
             
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection(db_collection);

        Document document = collection.find(eq("address", address)).first();
        if (document == null) {             
            System.out.println("ReadDB_WAVESWalletByAddress> Nothing was found in DB");
            mongoClient.close(); 
            return null;
        }; 
        
        System.out.println("ReadDB_WAVESWalletByAddress> A record was found in DB");         
        JSONObject obj = new JSONObject(document.toJson());    
        mongoClient.close(); 

        return obj;
    } 
}