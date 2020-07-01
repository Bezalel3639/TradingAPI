package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
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
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import com.bezalel.trading_api.Utils.TrippleDes;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Ethereum_web3j {
    
    Settings settings = new Settings();
    
    public BigInteger getLastBlock()
    {
        BigInteger lastblock = null;
        
        try {		    
            Web3j web3 = Web3j.build(new HttpService(settings.url_mainnet));		
            EthBlockNumber blockNumber = web3.ethBlockNumber().send();
            lastblock = blockNumber.getBlockNumber();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return lastblock;	
    }
		
    public double getETHRate() throws Exception {
    	
        String uri = settings.url_quotes;
        double rate = 0;
            
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("symbol","ETH")); 
        
        try {
            String result = makeAPICall(uri, parameters);
            JSONObject obj = new JSONObject(result);
            JSONObject objnext = obj.getJSONObject("data");
            objnext = objnext.getJSONObject("ETH");
            objnext = objnext.getJSONObject("quote");
            objnext = objnext.getJSONObject("USD");
            rate = objnext.getDouble("price");   	    
            System.out.println(rate); 
        } catch (IOException e) {
            System.out.println("Error: cannont access content - " + e.toString());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.toString());
        }
    
        return rate;	  
    }
	
    public String makeAPICall(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
    
        String apiKey = settings.apiKey; 
        String response_content = "";
        
        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);
        
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(query.build());
          
        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", apiKey);
        
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


    public BigInteger getBalance(String address) throws Exception {
    	
        String url = settings.url_mainnet;
    	BigInteger balance = null;
        
    	try {
            Web3j web3 = Web3j.build(new HttpService(url));
            EthGetBalance getBalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            balance = getBalance.getBalance();
    	}
    	catch (Exception e) {
            e.printStackTrace();
    	}
    	
    	return balance;
    }
	
    public boolean isValidAddress(String address) throws Exception {    	
        return WalletUtils.isValidAddress(address);
    }
    
    public JSONObject getNewAddress(String user, String password) throws Exception {
        String network = "Mainnet";
        String db_name = "wallets"; 
        Web3j web3 = Web3j.build(new HttpService(settings.url_mainnet));
        String store = null;
        Utils utils = new Utils();
        
        // Validate DB connection 
        JSONObject result = null; 
        JSONObject connect_result = new JSONObject();
        connect_result = validateMongoDBConnection(settings.user, db_name, settings.password);        
        if (connect_result.get("status").equals("-1")) {          
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The operation rejected: " + connect_result.get("data").toString());
            return result;
        } else {
            System.out.println("GetNewAddress> " + connect_result.get("data").toString());  
        }
        
        // Prepare Web3j
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        System.out.println("GetNewAddress>" + web3ClientVersion.getWeb3ClientVersion()); 
        
        String OS = System.getProperty("os.name"); // Windows 10
        
        if (OS.equals("Windows 10")) {
            store = settings.win10_store;
        }
        else if (OS.equals("Linux")) {
            store = settings.linux_store;
        }
        else {
            System.out.println("GetNewAddress> The OS is not supported"); 
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The OS is not supported");
            return result;
        }      
        
        // Generate wallet file
        String fileName = null;
        try {
            fileName = WalletUtils.generateNewWalletFile(password, new File(store));
            System.out.println("GetNewAddress> " + fileName);
            System.out.println("GetNewAddress> WalletUtils.generateNewWalletFile succeeded!"); 
        } catch (Exception e) {
            System.out.println("GetNewAddress> WalletUtils.generateNewWalletFile generated exception!"); 
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "WalletUtils.generateNewWalletFile generated exception!");
            return result;
        }
        
        // Get address from wallet name 
        int last_hyphen = fileName.lastIndexOf("-"); 
        int last_dot = fileName.lastIndexOf("."); 
        String address = "0x" + fileName.substring(last_hyphen+1, last_dot);
        System.out.println("GetNewAddress> A newaddress from the wallet name: " + address); 
        
        // Get wallet data 
        String walletfile = null;
        String walletdata = null;
        if (OS.equals("Windows 10")) {
            walletfile = store + "\\" + fileName;
        }
        else if (OS.equals("Linux")) {
            walletfile = store + "/" + fileName;            
        }        
        System.out.println("GetNewAddress> walletfile : " + walletfile);
        File file = new File(walletfile);
        file.setReadable(true);
        if (!file.exists()) {
            System.out.println("GetNewAddress> The file " + walletfile + " is not found!");
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The file " + walletfile + " is not found!");
            return result;
        }
        if (file.canRead()) {
            file.setReadable(true);
            walletdata = FileUtils.readFileToString(file, "UTF-8");
            System.out.println("GetNewAddress> The file " + walletfile + " is readable!");
            System.out.println("GetNewAddress> The wallet data: " + walletdata);
        } else {
            System.out.println("GetNewAddress> No permissions to read the file " + walletfile + "!");
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "No permissions to read the file " + walletfile + "!");
            return result;
        }
        
        // Encrypt password
        Utils.TrippleDes td = utils.new TrippleDes();
        String password_encrypted = td.encrypt(password);    
        // Prepare JSON for DB
        String json = utils.makeJSONFromWallet(address, password_encrypted, network, user, fileName, walletdata);
        System.out.println("GetNewAddress> result: " + json);
                
        // Store new wallet into MongoDB             
        try {
            Settings settings = new Settings();
            MongoCredential credential = MongoCredential.createCredential(
                    settings.user, 
                    db_name, 
                    settings.password.toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(
                    Settings.mongodb_host, 
                    settings.mongodb_port),
                    Arrays.asList(credential));
            
            Document doc = Document.parse(json);
            mongoClient.getDatabase(db_name).getCollection("ETH").insertOne(doc);
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        result = new JSONObject();
        result.put("status", "200");
        result.put("data", address);
        return result;
    }
 
    public JSONObject sendETH2ETH(double amount, String addressfrom, String addressto) throws Exception {

        String asset = "ETH";  
        int gas_limit = 21000;
        JSONObject result = null; 
        Utils utils = new Utils();
        
        // Validate DB connection 
        String db_name = "wallets";  
        JSONObject connect_result = new JSONObject();
        connect_result = validateMongoDBConnection(settings.user, db_name, settings.password);        
        if (connect_result.get("status").equals("-1")) {          
            result = new JSONObject();
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
            result = new JSONObject();
            result.put("status", "-1");
            result.put("data", "The source address " + addressfrom + " does not exist!");
            return result;
        } else {             
            System.out.println("SendETH2ETH> The source address " + addressfrom + " is valid!");
        }       
        System.out.println("SendETH2ETH> Validation of the destination address...");
        if (!isValidAddress(addressto)) {
            System.out.println("SendETH2ETH> The destination address does not exist");
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The destination address " + addressfrom + " does not exist!");
            return result;
        } else {             
            System.out.println("SendETH2ETH> The destination address " + addressto + " is valid!");
        }
        
        // Validate if the transfer amount is inside min/max range
        Settings settings = new Settings();
        JSONObject object;
        object = settings.getTransferRanges(asset);
        double min = object.getDouble("min");
        double max = object.getDouble("max");
        System.out.println("SendETH2ETH> Validation of the transaction range...");
        System.out.println("SendETH2ETH> The min/max transfer value is : " + min + "/" + max + " ETH");
        if ( amount >= min && amount <= max) {
            System.out.println("SendETH2ETH> The transfer amount " + amount + " is inside the range!");
        } else if (amount < min) {
            System.out.println("SendETH2ETH> The transaction rejected! The minimumun amount of transaction must be more or equal " + min + " ETH!");  
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the minimumun amount of transaction must be more or equal " + min + " ETH!");
            return result;
        } else if (amount > max) {
            System.out.println("SendETH2ETH> The transaction rejected: the maximum amount of transaction must be less or equal " + max + " ETH!");  
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: the maximum amount of transaction must be less or equal " + max + " ETH!");
            return result;
        }
         
        // Prepare for the source balance validation. Get source balance.
        BigInteger balance = getBalance(addressfrom);
        System.out.println("SendETH2ETH> The source balance: " + balance + " WEI or " + Convert.fromWei(balance.toString(), Convert.Unit.ETHER)  + " ETH"); // 2856699995107000 WEI
        
        // Estimate network fee for transfer. Estimated gas price unit is ETH.
        // The "transaction fee" is "gas limit" x "gas price", where gas price  
        // unit is GWEI. Since Transfer.sendFunds is used for transfer, the   
        // default network gas limit, gas price are used, which are 21000 (the  
        // standard gas limit) and a fluctuating value ("standard" gas price of 
        // ethgasstation.info) respectively.
        long gas_price = Settings.eth_gas_price*1000000000L; // GWEI to WEI, 1 GWEI = 10^9 WEI 
        BigDecimal estimated_fee = Convert.fromWei(Long.toString(gas_limit*gas_price), Convert.Unit.ETHER); // Estimated fee: 0.00084 (eth_gas_price: 40 GWEI)
        System.out.println("SendETH2ETH> gas limit: " + gas_limit + ", gas price: " + Settings.eth_gas_price + " GWEI. Note: the gas price might differ from the network value!"); 
        System.out.println("SendETH2ETH> Check if the system gas limit is 21000: " + Transfer.GAS_LIMIT.toString());
        System.out.println("SendETH2ETH> The estimated network fee: " + estimated_fee);
        
        // Validate source balance 
        double estimated_withdrawal_amount = amount + estimated_fee.doubleValue();
        BigDecimal sourceBalanceEther = Convert.fromWei(balance.toString(), Convert.Unit.ETHER);
        if (estimated_withdrawal_amount /* + plus Fintech Gateway transfer fee */ > sourceBalanceEther.doubleValue()) {
            System.out.println("SendETH2ETH> Estimated amount of transfer including fee: " + estimated_withdrawal_amount + ", Source amount: " + sourceBalanceEther.doubleValue());
            System.out.println("SendETH2ETH> The transaction rejected: insufficient funds!");
            result = new JSONObject();           
            result.put("status", "-1");
            result.put("data", "The transaction rejected: insufficient funds! The source amount: " + sourceBalanceEther.doubleValue() + ", the estimated amount of transfer including fee: " + estimated_withdrawal_amount);
            return result;
        }
        
        // Validate availability of wallet corresponding to the source address in MongoDB
        JSONObject dbwallet = readDB_ETHWalletByAddress(addressfrom); // TODO: probably add collection param and use from ExchangeSandbox.java
        String encrypted_password = dbwallet.getString("password");
        System.out.println("SendETH2ETH> The source wallet is found! The encrypted password: " + encrypted_password);
        
        Utils.TrippleDes td = utils.new TrippleDes();
        String password = td.decrypt(encrypted_password);
        
        // Validate availability of the original wallet data 
        String walletfile = null;
        JSONObject wallet = dbwallet.getJSONObject("wallet");
        JSONObject data = wallet.getJSONObject("data");
        if (data == null) {
            System.out.println("SendETH2ETH> THe wallet data is not found!");
            result = new JSONObject();            
            result.put("status", "-1");
            result.put("data", "The wallet data is not found!");
            return result;
        }
        // Get original wallet file from DB object
        System.out.println("SendETH2ETH> The original wallet data is found!"); 
        walletfile = data.toString();
        
        // All validations successfully completed
        System.out.println("SendETH2ETH> The transfer is approved! The estimated amount of transfer including fee: " + estimated_withdrawal_amount);
        
        // Prepare transfer transaction. Use temporarily file on the disk to be 
        // able to use WalletUtils.loadCredential.
        File temp = null;
        try {
            temp = File.createTempFile("data", ".tmp");
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));
            out.write(walletfile);
            out.close();
        } catch (IOException e) {
            System.out.println("SendETH2ETH> Error while creating temp file");
            result = new JSONObject();            
            result.put("status", "-1");
            result.put("data", "An error while creating temp file");
            return result;
        } 
        System.out.println("SendETH2ETH> Temp file: " + temp.getAbsolutePath());
        
        // Get credentials of the source wallet
        Credentials credentials = null;
        Web3j web3 = Web3j.build(new HttpService(settings.url_mainnet));
       
        try {
            credentials = WalletUtils.loadCredentials(password, temp.getAbsolutePath());
            System.out.println("SendETH2ETH> The source wallet is loaded...");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            result = new JSONObject(); 
            result.put("status", "-1");
            result.put("data", "An error while loading source wallet");
            return result;   
        }
        
        // Credentials check
        System.out.println("SendETH2ETH> The source address: " + credentials.getAddress());
      
        
        long startTime = System.currentTimeMillis();
        BigDecimal amountInWei = Convert.toWei(Double.toString(amount), Convert.Unit.ETHER);
        System.out.println("SendETH2ETH> amountInWei: " + amountInWei);
        TransactionReceipt transactionReceipt = null;
        try {
           System.out.println("SendETH2ETH> The transfer transaction is running...");
           transactionReceipt = Transfer.sendFunds(web3, 
                                                   credentials, 
                                                   addressto,  
                                                   amountInWei, 
                                                   Convert.Unit.WEI).send();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            result = new JSONObject(); 
            result.put("status", "-1");
            result.put("data", "An error while executing transaction with Transfer.sendFunds");
            return result; 
        }      
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("SendETH2ETH> The transfer is successfully completed!");
        System.out.println("SendETH2ETH> TransactionReceipt: " + transactionReceipt.toString());
        System.out.println("SendETH2ETH> Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);
    
        // Log transaction results
        int execution_time = (int) (((double) elapsedTime)/1000); // sec
        String log = utils.logTransferTXResults(amount, addressfrom, addressto, transactionReceipt.toString(), execution_time);
        System.out.println("SendETH2ETH> The transaction log added to MongoDB: " + log);
        
        result = new JSONObject(); 
        result.put("status", "200");
        result.put("data", "The transaction successfully executed for " + execution_time + " sec!");
        return result; 
    }
    
   public JSONObject readDB_ETHWalletByAddress(String address) throws Exception {
        
        Settings settings = new Settings();
        
        String db_name = "wallets";
        MongoCredential credential = MongoCredential.createCredential(
                settings.user, 
                db_name, 
                settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(
                Settings.mongodb_host, 
                settings.mongodb_port),
                Arrays.asList(credential)); 
             
        MongoDatabase database = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = database.getCollection("ETH");

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
   
   public JSONObject validateMongoDBConnection(String user, String database, String password) throws Exception {        
       Settings settings = new Settings();
       JSONObject result = null;         

       MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
       MongoClient mongoClient = null;
       
       long startTime = System.currentTimeMillis();
       try {
           mongoClient = new MongoClient(new ServerAddress(Settings.mongodb_host, settings.mongodb_port), Arrays.asList(credential));
           mongoClient.getAddress();
       } catch (MongoException e) {
           System.out.println("ValidateMongoDBConnection> Server is unavailable!" + e.getMessage());
           mongoClient.close();
           result = new JSONObject(); 
           result.put("status", "-1");
           result.put("data", "server is unavailable!");
           return result;
       }
       mongoClient.close();
       long stopTime = System.currentTimeMillis();
       long elapsedTime = stopTime - startTime;
       System.out.println("ValidateMongoDBConnection> Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);

       result = new JSONObject(); 
       result.put("status", "200");
       result.put("data", "Database connection is verified!");
       return result;    
   }
}