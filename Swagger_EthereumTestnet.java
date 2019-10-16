package com.bezalel.trading_api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1")
@Api(value = "Ethereum Endpoints")
public class Swagger_EthereumTestnet {
	
    @RequestMapping(path="/Ethereum/Testnet/LastBlock/", method = RequestMethod.GET)
    @ApiOperation(value="Get last block", tags="Ethereum sandbox API")
    public BigInteger getLastBlock() {
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
        BigInteger result = eth.getLastBlock();
          
        return result; 
    }
    
    @RequestMapping(path="/Ethereum/Testnet/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Ethereum sandbox API")
    public BigInteger getBalance(
            @PathVariable 
            @ApiParam(defaultValue = "0x0d6f2055eedae95ccd888661aa5e3c05c1aeb64d") String address) 
            throws Exception {
    
    	EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
     	BigInteger result = eth.getBalance(address);
   
     	return result; 
    }

    @RequestMapping(path="/Ethereum/Testnet/IsAddressValid/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Check if address is valid", tags="Ethereum sandbox API")
    public boolean IsAddressValid(
            @PathVariable 
            @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String address) throws Exception {

        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
        return eth.IsValidAddress(address); 
    }
    
    @RequestMapping(path="/Ethereum/Testnet/GetNewAddress/{user}/{password}", method = RequestMethod.GET)
    @ApiOperation(value="Generate new Ethreum testnet address", tags="Ethereum sandbox API")
    public ResponseEntity<String> GetNewAddress(
            @PathVariable  @ApiParam(defaultValue = "Ronaldo") String user, 
            @PathVariable 
            @ApiParam(defaultValue = "3****7") String password) throws Exception {

        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
        String result = eth.GetNewAddress(user, password); 
        
        if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }      
    }
    
    @RequestMapping(path="/Ethereum/Testnet/Send/{walletfrom}/{addressto}/{amount}/{password}", method = RequestMethod.POST)
    @ApiOperation(value="Send test ETH", tags="Ethereum sandbox API")
    public ResponseEntity<String> sendETHfromWallet(
            @PathVariable String walletfrom, 
            @PathVariable @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String addressto, 
            @PathVariable double amount,
            @PathVariable String password) throws Exception { 
        
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
        
        // Validate source wallet (must include address and end with .json)
        if (walletfrom.length() < 45) // 40 is the length of ETH address without "0x"
            return new ResponseEntity<>("The wallet name is invalid", HttpStatus.NOT_FOUND);
        if (!walletfrom.substring(walletfrom.length()-5, walletfrom.length()).equals(".json")) 
             return new ResponseEntity<>("The wallet name is invalid", HttpStatus.NOT_FOUND);
        
        // Validate destination address
        if (!eth.IsValidAddress(addressto))
            return new ResponseEntity<>("The destination address does not exist", HttpStatus.NOT_FOUND);
        
        // Get encryption tools
        ExchangeSandbox es = new ExchangeSandbox();
        Utils utils = new Utils();
        Utils.TrippleDes td = utils.new TrippleDes();
   
        // Get wallet object from DB        
        JSONObject dbwallet = null;
        dbwallet = es.readDB_ETHWallet(walletfrom);
        if (dbwallet == null)
          return new ResponseEntity<>("The wallet " + walletfrom + " was not found", HttpStatus.NOT_FOUND);
          
        // Validate password
        String encrypted_password = dbwallet.getString("password");
        System.out.println("SendETH> Encrypted password: " + encrypted_password + ", Decrypted password: " + td.decrypt(encrypted_password));
        if (!td.decrypt(encrypted_password).equals(password))
            return new ResponseEntity<>("The password is not valid", HttpStatus.FORBIDDEN);
       
        // Get original wallet file from DB object
        String walletfile = null;
        JSONObject wallet = dbwallet.getJSONObject("wallet");
        JSONObject data = wallet.getJSONObject("data");
        if (data == null)
            return new ResponseEntity<>("The wallet data not found", HttpStatus.NOT_FOUND);
        walletfile = data.toString();
        
        // Use temporarily file on the disk to enable using WalletUtils.loadCredential
        File temp = null;
        try {
            temp = File.createTempFile("data", ".tmp");
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));
            out.write(walletfile);
            out.close();
        } catch (IOException e) {
            return new ResponseEntity<>("Error while creating temp file", HttpStatus.FORBIDDEN);
        }
        
        System.out.println("SendETH> original wallet file: " + walletfile); 
        
        // Send ETH
        String result = eth.SendETH(temp.getAbsolutePath(), password, addressto, amount);
        System.out.println("Send> temp file location: " + temp.getAbsolutePath());
        temp.delete(); 
        
        // Validate results
        if (result.equals("404")) {
            return new ResponseEntity<>("The wallet not found: " + walletfrom, HttpStatus.NOT_FOUND); 
        } else if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }  
    } 
    
    @RequestMapping(path="/Ethereum/Testnet/SendFromAddress/{addressfrom}/{addressto}/{amount}/{password}", method = RequestMethod.POST)
    @ApiOperation(value="Send test ETH", tags="Ethereum sandbox API")
    public ResponseEntity<String> sendETHfromAddress(
        @PathVariable @ApiParam(defaultValue = "0x0d6f2055eedae95ccd888661aa5e3c05c1aeb64d") String addressfrom, 
        @PathVariable @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String addressto, 
        @PathVariable @ApiParam(defaultValue = "0.02") double amount,
        @PathVariable String password) throws Exception { 
        
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
        
        // Validate addresses
        if (!eth.IsValidAddress(addressfrom))
            return new ResponseEntity<>("The source address does not exist", HttpStatus.NOT_FOUND);
        if (!eth.IsValidAddress(addressto))
            return new ResponseEntity<>("The destination address does not exist", HttpStatus.NOT_FOUND);
         
        // Get encryption tools
        ExchangeSandbox es = new ExchangeSandbox();
        Utils utils = new Utils();
        Utils.TrippleDes td = utils.new TrippleDes();
   
        // Get wallet object from DB
        JSONObject dbwallet = es.readDB_ETHWalletByAddress(addressfrom);
        String encrypted_password = dbwallet.getString("password");
        
        if (dbwallet.has("ERROR")) 
            return new ResponseEntity<>("The wallet for the address " + addressfrom + " was not found", 
                HttpStatus.NOT_FOUND);              
         
        // Validate password
        if (!td.decrypt(encrypted_password).equals(password))
            return new ResponseEntity<>("The password is not valid", HttpStatus.FORBIDDEN);
   
        // Get original wallet file from DB object
        String walletfile = null;
        JSONObject wallet = dbwallet.getJSONObject("wallet");
        JSONObject data = wallet.getJSONObject("data");
        if (data == null)
            return new ResponseEntity<>("The wallet data not found", HttpStatus.NOT_FOUND);
        walletfile = data.toString();
        
        // Use temporarily file on the disk to be able to use WalletUtils.loadCredential
        File temp = null;
        try {
            temp = File.createTempFile("data", ".tmp");
            BufferedWriter out = new BufferedWriter(new FileWriter(temp));
            out.write(walletfile);
            out.close();
        } catch (IOException e) {
            return new ResponseEntity<>("Error while creating temp file", HttpStatus.FORBIDDEN);
        }  
       
        // Send ETH
        String result = eth.SendETH(temp.getAbsolutePath(), password, addressto, amount); 
        System.out.println("SendETHfromAddress> temp file location: " + temp.getAbsolutePath());
        temp.delete(); 
        
        // Validate results
        if (result.endsWith("404")) {
            return new ResponseEntity<>("The wallet not found: " + walletfile, HttpStatus.NOT_FOUND); 
        } else if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }  
    }
}
