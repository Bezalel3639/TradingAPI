package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    
    public String getNewAddress(String user, String password) throws Exception {
        String network = "Mainnet";
        Web3j web3 = Web3j.build(new HttpService(settings.url_mainnet));
        String store = null;
        Utils utils = new Utils();
        
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
            return "403"; // forbidden
        }      
        
	// Generate wallet file
	String fileName = WalletUtils.generateNewWalletFile(password, new File(store));
	System.out.println(fileName);

	String walletfile = null;
	if (OS.equals("Windows 10")) {
	   walletfile = store + "\\" + fileName;
	}
	else if (OS.equals("Linux")) {
	   walletfile = store + "/" + fileName;
	}

	Credentials credentials = WalletUtils.loadCredentials(password, walletfile);
	String address = credentials.getAddress();
	System.out.println(address); 

	// Encrypt password
	Utils.TrippleDes td = utils.new TrippleDes();
	String password_encrypted = td.encrypt(password);
	// Get wallet data
	String walletdata = utils.readFile(walletfile );        
	// Prepare JSON for DB
	String json = utils.makeJSONFromWallet(address, password_encrypted, network, user, fileName, walletdata);

	// Store new wallet into MongoDB             
	try {
	   Settings settings = new Settings();
	   String db_name = "wallets";
	   MongoCredential credential = MongoCredential.createCredential(
	           settings.user, 
	           db_name, 
	           settings.password.toCharArray());
	   MongoClient mongoClient = new MongoClient(new ServerAddress(
	           settings.mongodb_host, 
	           settings.mongodb_port),
	           Arrays.asList(credential));
	   
	   Document doc = Document.parse(json);
	   mongoClient.getDatabase(db_name).getCollection("ETH").insertOne(doc);
	   mongoClient.close();
	} catch (Exception e) {
	   e.printStackTrace();
	}
        
        return "Debugging new address failure"; 
        //return address;  
    }
 
    public JSONObject sendETH2ETH(double amount, String addressfrom, String addressto) throws Exception {

        String asset = "ETH";  
        int gas_limit = 21000;
        JSONObject result = null;       
        
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
        // SendETH2ETH> The source balance: 2856699995107000 WEI or 0.002856699995107 ETH
        System.out.println("SendETH2ETH> The source balance: " + balance + " WEI or " + Convert.fromWei(balance.toString(), Convert.Unit.ETHER)  + " ETH"); // 2856699995107000 WEI
        
        // Estimate network fee for transfer. Estimated gas price unit is ETH.
        // The "transaction fee" is "gas limit" x "gas price", where gas price  
        // unit is GWEI. Since Transfer.sendFunds is used for transfer, the   
        // default network gas limit, gas price are used, which are 21000 (the  
        // standard gas limit) and fluctuating value ("standard" gas price of 
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
        
        Utils utils = new Utils();
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
        Utils utililities = new  Utils();
        String log = utililities.logTransferTXResults(amount, addressfrom, addressto, transactionReceipt.toString(), execution_time);
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
}






