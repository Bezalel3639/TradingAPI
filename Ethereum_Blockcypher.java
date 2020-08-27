package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bson.Document;
import org.codehaus.jettison.json.JSONObject;
import org.web3j.crypto.WalletUtils;

import com.bezalel.trading_api.Utils.TrippleDes;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class Ethereum_Blockcypher {
    
    Settings settings = new Settings();
    
    public int getLastBlock() throws Exception {
        
        Settings settings = new Settings();
        URL url = new URL (settings.url_eth_mainnet);
        int ETCheight;

        String json = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                json=json+line;             
            }
            JSONObject obj = new JSONObject(json);
            ETCheight = obj.getInt("height");
        }
        System.out.println(ETCheight);
            
        return ETCheight;
    }
    
    public boolean isValidAddress(String address) throws Exception {        
        return WalletUtils.isValidAddress(address);
    }
    
    public long getBalance(String address) throws Exception {
        
        long balance = 0; 
        String json = "";
        
        String endpoint = settings.url_eth_addr_mainnet + "/" + address + "/balance";
        URL url = new URL (endpoint);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                json=json+line;
            }
            JSONObject obj = new JSONObject(json);
            balance = obj.getLong("balance"); 
            System.out.println("GetBalance> " + balance);
            System.out.println(json);
        }  
        
        return balance;
    } 
    
    public org.json.JSONObject getNewAddress(String user_email, String notes) throws Exception {
        
        String network = "Mainnet";
        String db_name = "wallets";
        String collection = "ETH";
        String json = "";
        Utils utils = new Utils();
        
        // Validate DB connection
        org.json.JSONObject result = null;
        System.out.println("GetNewAddress> Validation of DB connection...");
        org.json.JSONObject connect_result = new org.json.JSONObject();
        connect_result = utils.validateMongoDBConnection(settings.user, db_name, settings.password);  
        if (connect_result.get("status").equals("-1")) {          
            result = new org.json.JSONObject();
            result.put("status", "-1");
            result.put("data", "The operation rejected: " + connect_result.get("data").toString());
            return result;
        } else {
            System.out.println("GetNewAddress> " + connect_result.get("data").toString());  
        }       
        
        HttpClient httpclient = HttpClients.createDefault(); 
        HttpPost httppost = new HttpPost(settings.url_eth_addr_mainnet);

        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair("token", settings. BTCLTCETH_apikey));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        
        if (entity != null) {
            try (InputStream instream = entity.getContent()) {              
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                for (String line; (line = reader.readLine()) != null;) {
                    json=json+line;
                }
            }
        } 
        
        if (json == "") {
            result = new org.json.JSONObject();
            result.put("status", "-1");
            result.put("data", "Failuire to to generate ETH address!");
            return result;
        }       
        System.out.println("GetNewAddress> New ETH wallet: " + json);       
        
        // Prepare JSON wallet data for DB
        JSONObject wallet = new JSONObject(json);
        String address = wallet.get("address").toString();
        System.out.println("GetNewAddress> A new ETH address: " + wallet.get("address").toString());
        
        // Encrypt seed and wallet data
        Utils.TrippleDes td = utils.new TrippleDes();
        String pk_encrypted = td.encrypt(wallet.get("private").toString());       
        String walletdata_encrypted = td.encrypt(json);
        
        // Prepare JSON with all data for DB
        String wallet_json = utils.makeJSON_ETHBlockcypherWallet(
                address, 
                pk_encrypted, 
                network, 
                user_email, 
                walletdata_encrypted, 
                settings.eth_provider, 
                notes);
        System.out.println("GetNewAddress> ETH wallet data: " + wallet_json);
        
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
            result = new org.json.JSONObject();
            result.put("status", "-1");
            result.put("data", "The operation rejected: failure to enter account data for address " + address + " into DB!");
            return result;
        }
        
        result = new org.json.JSONObject();
        result.put("status", "200");
        result.put("data", address);
        return result;
    }
    
    public org.json.JSONObject sendETH2ETH(
             String addressfrom, 
             String addressto, 
             double amount) throws Exception { 
        
        String url = settings.url_eth_newtx_mainnet;
        String asset = "ETH"; 
        long fee = 10300; 
        long eth2wei = 1_000_000_000_000_000_000L; // 1 ETH = 10^18 wei
          org.json.JSONObject result = null; 
        Utils utils = new Utils();
        String private_key = null;
        String public_key = null;
        
        // Validate DB connection 
        long startTimeCumulative = System.currentTimeMillis();
        System.out.println("SendETH2ETH> Validation of DB connection...");
        String db_name = "wallets"; 
        org.json.JSONObject connect_result = new org.json.JSONObject();
        connect_result = utils.validateMongoDBConnection(settings.user, db_name, settings.password);        
        if (connect_result.get("status").equals("-1")) {          
            result = new org.json.JSONObject();
            result.put("status", "-1");
            result.put("data", "The transaction rejected: " + connect_result.get("data").toString());
            return result;
        } else {
            System.out.println("SendETH2ETH> " + connect_result.get("data").toString());  
        }
        
        // Validate addresses
        System.out.println("SendETH2ETH> Validation of the source address...");
        if (!isValidAddress(addressfrom)) {
            System.out.println("SendETH2ETH> The source address " + addressfrom + " does not exist!");
            result = new org.json.JSONObject();
            result.put("status", "-1");
            result.put("data", "The source address " + addressfrom + " does not exist!");
            return result;
        } else {             
            System.out.println("SendETH2ETH> The source address " + addressfrom + " is valid!");
        }       
        System.out.println("SendETH2ETH> Validation of the destination address...");
        if (!isValidAddress(addressto)) {
            System.out.println("SendETH2ETH> The destination address does not exist");
            result = new org.json.JSONObject();           
            result.put("status", "-1");
            result.put("data", "The destination address " + addressto + " does not exist!");
            return result;
        } else {             
            System.out.println("SendETH2ETH> The destination address " + addressto + " is valid!");
        }
        
        // Remove 0x prefix from the source address if exists (0x-less address is required when validating wallet availability) 
        if (addressfrom.indexOf("0x") != -1) {
            addressfrom = addressfrom.substring(2);
            System.out.println("SendETH2ETH> The source address is modified: " + addressfrom + " (0x is removed)");
        }
        // This is only for consistency with format of the source address
        if (addressto.indexOf("0x") != -1) { 
            addressto = addressto.substring(2);
            System.out.println("SendETH2ETH> The destination address is modified: " + addressto + " (0x is removed)");
        }
        
        // Validate if the transfer amount is inside min/max range
        org.json.JSONObject object = settings.getTransferRanges(asset);
        double min = object.getDouble("min");
        double max = object.getDouble("max");
        System.out.println("SendETH2ETH> Validation of the transaction range...");
        System.out.println("SendETH2ETH> The min/max transfer value is " + min + "/" + max + " ETH");
        if ( amount >= min && amount <= max) {
            System.out.println("SendETH2ETH> The transfer amount " + amount + " is inside the range!");
        } else if (amount < min) {
            System.out.println("SendETH2ETH> The transaction rejected! The minimumun amount of transaction must be more or equal " + min + " ETH!");  
            result = new org.json.JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the minimumun amount of transaction must be more or equal " + min + " ETH!");
            return result;
        } else if (amount > max) {
            System.out.println("SendETH2ETH> The transaction rejected: the maximum amount of transaction must be less or equal " + max + " ETH!");  
            result = new org.json.JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the maximum amount of transaction must be less or equal " + max + " ETH!");
            return result;
        }
        
        // Validate source balance 
        System.out.println("SendETH2ETHS> Validation of the source balance...");
        long userbalance = getBalance(addressfrom);
        long estimated_withdrawal_amount = (long)(amount /* ETH */ * eth2wei) + fee;
        if (estimated_withdrawal_amount /* + plus Fintech Gateway transfer fee */ > userbalance) {
            System.out.println("SendETH2ETH> Estimated amount of transfer including fee: " + estimated_withdrawal_amount + ", Source amount: " + userbalance);
            System.out.println("SendETH2ETH> The transaction rejected: insufficient funds!");
            result = new org.json.JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: insufficient funds! The source amount: " + userbalance + ", the estimated amount of transfer including fee: " + estimated_withdrawal_amount);
            return result;
        } else {
            System.out.println("SendETH2ETH> The source balance " + userbalance + " is valid for transaction!");  
        }
        
        // Validate availability of wallet corresponding to the source address in MongoDB
        System.out.println("SendETH2ETH> Validation of the source wallet availability in MongoDB...");
        JSONObject obj = readDB_ETHWalletByAddress(addressfrom); 
        if (obj == null) {            
            result = new org.json.JSONObject();
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the wallet was not found in the DB");
            return result;            
        } else {
            System.out.println("SendETH2ETH> The source wallet is found in MongoDB!");
            System.out.println("SendETH2ETH> The encrypted wallet: " + obj.get("wallet")); 
            String encrypted_wallet = obj.get("wallet").toString();
            Utils.TrippleDes td = utils.new TrippleDes();
            org.json.JSONObject wallet = new org.json.JSONObject(td.decrypt(encrypted_wallet));
            private_key = wallet.getString("private");
            public_key = wallet.getString("public");
            System.out.println("SendETH2ETH> The public key: " + public_key);
        }
        
        // All validations successfully completed
        System.out.println("SendETH2ETH> The transfer is approved! The estimated amount of transfer including fee: " + estimated_withdrawal_amount);
        
        // Run TX, step1:  build transaction string, send to URL, and read "tosign" value from the returned json
        OkHttpClient client = new OkHttpClient();
        String content_1 = "{\"inputs\": [{\"addresses\": [\"";
        String content_2 = "\"]}], \"outputs\": [{\"addresses\": [\"";
        String content_3 = "\"], \"value\": ";
        String content_4 = "}]}";
        String content = content_1 + addressfrom + content_2 + addressto + content_3 + String.valueOf ((long)(amount /* ETH */ * eth2wei)) + content_4;
        System.out.println("SendETH2ETH> content: " + content);

        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = RequestBody.create(mediaType, content);       
        
        // Blockcypher.com's specific URL for ETH (no token for BTC, LTC)
        url = url + "?token=" + settings.BTCLTCETH_apikey;
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("cache-control", "no-cache")
            .build();        
        Response response = client.newCall(request).execute();
        
        String step1_response = response.body().string();
        System.out.println("SendETH2ETH> The JSON with tosign key: " + step1_response);
        String tosign_value = getData2Sign(step1_response);
        
        // Run TX, step2:  sign "tosign" value with the private key
        String signature = signData(tosign_value, private_key);
        System.out.println("SendETH2ETH> The signed data: " + signature);
        
        // Run TX, step3: append signature and public key, and run transaction
        JSONObject response_obj = new JSONObject(step1_response);
        String tx_result = null;
        long startTime = System.currentTimeMillis();
        try {
            tx_result = sendTX(response_obj, signature, public_key);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            result = new org.json.JSONObject(); 
            result.put("status", "-1");
            result.put("data", "An error while executing transaction!");
            return result;               
        }
        long stopTime = System.currentTimeMillis();
        long startStopCumulative = stopTime;
        long elapsedTime = stopTime - startTime;
        long elapsedTimeCumulative = startStopCumulative - startTimeCumulative;
        System.out.println("SendETH2ETH> The transfer is successfully completed!");
        System.out.println("SendETH2ETH> The transaction result: " + tx_result);
        System.out.println("SendETH2ETH> Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);
        System.out.println("SendETH2ETH> Cutulative time (millisec): " + elapsedTimeCumulative + ", Time (sec): " + ((double) elapsedTimeCumulative)/1000 + ", Time (min): " + ((double) elapsedTimeCumulative)/1000/60);

        // Log transaction results
        int cumulative_execution_time = (int) (((double) elapsedTimeCumulative)/1000); // sec
        String log = utils.logTransferTXResults("ETH2ETH", amount, addressfrom, addressto, tx_result, cumulative_execution_time);
        System.out.println("SendETH2ETH> The transaction log added to MongoDB: " + log);
        
        result = new org.json.JSONObject();
        result.put("status", "200");
        result.put("data", "The transaction successfully executed for " + cumulative_execution_time + " sec!");
        return result;
    }
    
    public String getData2Sign (String step1_response) throws Exception {
        String data;        
        
        JSONObject obj = new JSONObject(step1_response);
        System.out.println("GetData2Sign> JSONObject: " + obj.toString());
        
        data = obj.get("tosign").toString(); 
        data = data.substring(2, data.length()-2); 
        
        System.out.println("Data: " + data);
        return data;        
    }
    
    public String signData(String data, String pkey) throws Exception {
        
        String OS = System.getProperty("os.name");      
        Process process = null;

        if (OS.equals("Windows 10")) {
            process = new ProcessBuilder(settings.ffn_signer_windows, data, pkey).start();
        }
        else if (OS.equals("Linux")) {
            String tomcat = System.getProperty("catalina.home"); 
            String signer = tomcat + settings.ffn_signer_linux;
            System.out.println("Tomcat home: " + signer);
            process = new ProcessBuilder(signer, data, pkey).start(); 
        }
        else {
            System.out.println("The OS is not supported");      
        }

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
    
        String sdata = "";
        while ((line = br.readLine()) != null) {
           sdata = sdata + line;
        }
        
        return sdata;
    } 
    
    public String sendTX(JSONObject obj, String sdata, String pubkey) throws Exception { 
        
        String url = settings.url_eth_sendtx_mainnet;
        obj.append("signatures", sdata);
        obj.append("pubkeys", pubkey);
        System.out.println("New JSON: "+ obj.toString());
        
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = RequestBody.create(mediaType, obj.toString()); 
 
        // Blockcypher.com's specific URL with token for ETH (no token for BTC, LTC)
        url = url + "?token=" + settings.BTCLTCETH_apikey;
        Request request = new Request.Builder()
          .url(url)
          .post(body)
          .addHeader("cache-control", "no-cache")
          .build();

        Response response = client.newCall(request).execute();  
        String result = response.body().string();
        
        return result;
    }
    
    public JSONObject readDB_ETHWalletByAddress(String address) throws Exception {
        String db_name = "wallets";
        String db_collection = "ETH"; 
        
        MongoCredential credential = MongoCredential.createCredential(
                settings.user, 
                db_name, 
                settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(
                Settings.mongodb_host, 
                settings.mongodb_port),
                Arrays.asList(credential)); 
             
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection(db_collection);

        Document document = collection.find(eq("address", address)).first();
        if (document == null) {             
            System.out.println("ReadDB_ETHWalletByAddress> Nothing was found in DB");
            mongoClient.close(); 
            return null;
        }; 
        
        System.out.println("ReadDB_ETHWalletByAddress> A record was found in DB");         
        JSONObject obj = new JSONObject(document.toJson());    
        mongoClient.close(); 

        return obj;
    }
}