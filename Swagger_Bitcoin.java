package com.bezalel.trading_api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigInteger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1")
@Api(value = "Bitcoin Endpoints", description = "Bitcoin API")
public class Swagger_Bitcoin {
    
    @RequestMapping(path="/Bitcoin/IsAddressValid/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Check if address is valid", tags="Bitcoin API")
    public boolean IsAddressValid(@PathVariable String address) throws Exception {  
        Bitcoin_Blockcypher bitcoin = new Bitcoin_Blockcypher();
        return bitcoin.validateAddress(address); 
    }
    
    @RequestMapping(path="/Bitcoin/GetNewAddress/{email}/{notes}", method = RequestMethod.GET)
    @ApiOperation(value="Create new BTC address", tags="Bitcoin API")
    public ResponseEntity<String> getNewAddress(   
            @PathVariable String email,
            @PathVariable String notes) throws Exception {      
        
        Bitcoin_Blockcypher bitcoin = new Bitcoin_Blockcypher();
        org.json.JSONObject result  = bitcoin.getNewAddress(email, notes);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }   

    @RequestMapping(path="/Bitcoin/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Bitcoin API")
    public long getBalanceBlockcypher(@PathVariable String address) throws Exception {
        Bitcoin_Blockcypher bitcoin = new Bitcoin_Blockcypher();
        long result = bitcoin.getBalance(address);
 
        return result;
    }
    
    @RequestMapping(path="/Bitcoin/LastBlock/", method = RequestMethod.GET)
	@ApiOperation(value="Get last block", tags="Bitcoin API")
    public long getLastBlock() throws Exception {
     	long result = 0;
    	
  	  	Bitcoin_BlockchainInfo bitcoin = new Bitcoin_BlockchainInfo();
        result = bitcoin.getLastBlock();
 
    	return result; 
    }
    
    @RequestMapping(path="/Bitcoin/BlockchainInfo/BTCRate/", method = RequestMethod.GET)
	@ApiOperation(value="Get BTC rate in USD", tags="Bitcoin API")
    public String getBTCBlockchainInfoRate() throws Exception {
     	String result = null;
    	
  	  	Bitcoin_BlockchainInfo bitcoin = new Bitcoin_BlockchainInfo();
        result = bitcoin.getBTCRate();
 
    	return result; 
    }
    
    @RequestMapping(path="/Bitcoin/BTCRate/", method = RequestMethod.GET)
    @ApiOperation(value="Get BTC rate in USD", tags="Bitcoin API")
    public double getBTCRate() throws Exception {
        Bitcoin_Blockcypher bitcoin = new Bitcoin_Blockcypher();
        return bitcoin.getBTCRate();
    }
    
    @RequestMapping(path="/Bitcoin/SendFromAddress/", method = RequestMethod.POST)
    @ApiOperation(value="Send BTC", tags="Bitcoin API")
    public ResponseEntity<String> sendBTC(            
            @RequestParam(value="addressfrom", required=true) @ApiParam(defaultValue = "15CCuZX353pBagPdbLmCimFwQLNtAP82JX") String addressfrom,
            @RequestParam(value="addressto", required=true) @ApiParam(defaultValue = "1Dy7ZKCxKt51rDYkr6tjvNcX1ivuVQDFSC") String addressto, 
            @RequestParam(value="amount", required=true) @ApiParam(defaultValue = "0.0001") double amount,
            @RequestParam(value="password", required=true)  String password) throws Exception { 
    
        // Validate password
        Settings settings = new Settings();
        String true_password = settings.settings_password;
        System.out.println("True password: " + true_password + "Entered password: " + password);
        if (!true_password.equals(password))
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED); 
        
        Bitcoin_Blockcypher bitcoin = new Bitcoin_Blockcypher();
        org.json.JSONObject result = bitcoin.SendBTC2BTC(addressfrom, addressto, amount);
 
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    } 
}