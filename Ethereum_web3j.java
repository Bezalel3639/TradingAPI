package com.bezalel.trading_api;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import org.web3j.protocol.http.HttpService;

import com.mongodb.MongoClient;
import org.bson.Document;
import org.json.JSONObject;

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
	
    public boolean IsValidAddress(String address) throws Exception {    	
        return WalletUtils.isValidAddress(address);
    }
    
    public String GetNewAddress(String user, String password) throws Exception { 
        Web3j web3 = Web3j.build(new HttpService(settings.url_mainnet));
        String store = null;
        Utils utils = new Utils();
        
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        System.out.println(web3ClientVersion.getWeb3ClientVersion()); 
        
        String OS = System.getProperty("os.name"); // Windows 10
        
        if (OS.equals("Windows 10")) {
            store = settings.win10_store;
        }
        else if (OS.equals("Linux")) {
            store = settings.linux_store;
        }
        else {
            System.out.println("The OS is not supported"); 
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
        String walletdata = utils.ReadFile(walletfile );        
        // Prepare JSON for DB
        String json = utils.MakeJSONFromWallet(address, password_encrypted, user, fileName, walletdata);
        
        // Store new wallet into MongoDB             
        try {
            Settings settings = new Settings();
            MongoClient mongoClient = new MongoClient(settings.mongodb_host, settings.mongodb_port);
            Document doc = Document.parse(json);
            mongoClient.getDatabase("wallets").getCollection("ETH").insertOne(doc);
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return address;  
    }
}






