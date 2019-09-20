package com.bezalel.trading_api;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

public class EthereumTestnet_web3j {
	
    Settings settings = new Settings(); // Ropsten
    
    public BigInteger getLastBlock() {
    	
        BigInteger lastblock = null;
        
        try {		    
            Web3j web3 = Web3j.build(new HttpService(settings.url));		
            EthBlockNumber blockNumber = web3.ethBlockNumber().send();
            lastblock = blockNumber.getBlockNumber();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return lastblock;	
    }
    
    public BigInteger getBalance(String address) throws Exception {
    	
        BigInteger balance = null;
        
        try {
            Web3j web3 = Web3j.build(new HttpService(settings.url));
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
    
        Web3j web3 = Web3j.build(new HttpService(settings.url));
        String store = null;
        
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().send();
        System.out.println(web3ClientVersion.getWeb3ClientVersion()); 
        
        String OS = System.getProperty("os.name"); 
    
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
        
        return address;     
    }
    
    public String SendETH(
            String wallet, 
            String password, 
            String addressto, 
            double amount /* ETH */) throws Exception { 
        
        BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
        Credentials credentials = null;
        Web3j web3 = Web3j.build(new HttpService(settings.url));
       
        try {
            credentials = WalletUtils.loadCredentials(password, wallet);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "404"; // not found     
        } 

        long startTime = System.currentTimeMillis();
        TransactionReceipt transactionReceipt = Transfer.sendFunds(web3, credentials, 
            addressto, BigDecimal.valueOf(amount), Convert.Unit.ETHER).send(); 
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
 
        System.out.println(transactionReceipt.toString());
        BigInteger txFees = transactionReceipt.getGasUsed().multiply(GAS_PRICE);
        System.out.println("Fee: " + txFees); 
        System.out.println("Time (millisec): " + elapsedTime + ", Time (sec): " + ((double) elapsedTime)/1000 + ", Time (min): " + ((double) elapsedTime)/1000/60);
    
        return transactionReceipt.toString();
    }
}
