package com.bezalel.trading_api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigInteger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1")
@Api(value = "Ethereum Endpoints", description = "Ethereum API")
public class Swagger_Ethereum {

    @RequestMapping(path="/Ethereum/LastBlock/", method = RequestMethod.GET)
    @ApiOperation(value="Get last block", tags="Ethereum API")
    public BigInteger getLastBlock()
    {
        Ethereum_web3j eth = new Ethereum_web3j();
        BigInteger result = eth.getLastBlock();
        
        return result; 
    }
    
    @RequestMapping(path="/Ethereum/Blockcypher/LastBlock/", method = RequestMethod.GET)
    @ApiOperation(value="Get last block", tags="Ethereum API")
    public int getLastBlockBlockcypher() throws Exception {
        Ethereum_Blockcypher eth = new Ethereum_Blockcypher();
        int result = eth.getLastBlock();
        
        return result; 
    } 
    
    @RequestMapping(path="/Ethereum/ETHRate/", method = RequestMethod.GET)
    @ApiOperation(value="Get ETH rate in USD", tags="Ethereum API")
    public double getETHRate() throws Exception {    	
        Ethereum_web3j eth = new Ethereum_web3j();
        double result = eth.getETHRate();
        
        return result;    
    }    
   
    @RequestMapping(path="/Ethereum/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Ethereum API")
    public BigInteger getBalance(
            @PathVariable 
            @ApiParam(defaultValue = "0xe40a4a3ebfe28dcbf5613df090fce37bceaa4ae2") String address) 
            throws Exception {
   
        Ethereum_web3j eth = new Ethereum_web3j();
        BigInteger result = eth.getBalance(address);
  
        return result; 
    }
    
    @RequestMapping(path="/Ethereum/Blockcypher/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Ethereum API")
    public long getBalanceBlockcypher(
            @PathVariable 
            @ApiParam(defaultValue = "0xe40a4a3ebfe28dcbf5613df090fce37bceaa4ae2") String address) 
            throws Exception {
   
        Ethereum_Blockcypher eth = new Ethereum_Blockcypher();
        long result = eth.getBalance(address);
  
        return result; 
    } 
    
    @RequestMapping(path="/Ethereum/IsAddressValid/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Check if address is valid", tags="Ethereum API")
    public boolean isAddressValid(
            @PathVariable 
            @ApiParam(defaultValue = "65ab5cf3b2b701fb2f89fd10802f57c60da2470e") String address) 
            throws Exception {
        
        Ethereum_web3j eth = new Ethereum_web3j();  
        return eth.isValidAddress(address); 
    }   
   
    @RequestMapping(path="/Ethereum/GetNewAddress/{user}/{password}", method = RequestMethod.GET)
    @ApiOperation(value="Generate new Ethreum address", tags="Ethereum API")
    public ResponseEntity<String> getNewAddress(
            @PathVariable String user,
            @PathVariable String password) throws Exception {

        Ethereum_web3j eth = new Ethereum_web3j();  
        JSONObject result = eth.getNewAddress(user, password);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }
    
    @RequestMapping(path="/Ethereum/Blockcypher/GetNewAddress/{email}/{notes}", method = RequestMethod.GET)
    @ApiOperation(value="Generate new Ethreum address", tags="Ethereum API")
    public ResponseEntity<String> getNewAddressBlockcypher(
            @PathVariable String email,
            @PathVariable String notes) throws Exception {

        Ethereum_Blockcypher eth = new Ethereum_Blockcypher();  
        JSONObject result = eth.getNewAddress(email, notes);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }
    
    @RequestMapping(path="/Ethereum/SendFromAddress/{addressfrom}/{addressto}/{amount}/{password}", method = RequestMethod.POST)
    @ApiOperation(value="Send ETH", tags="Ethereum API")
    public ResponseEntity<String> sendETHfromAddress(
            @PathVariable @ApiParam(defaultValue = "0x9d2cfc32529ad2998150194199816e0c6f691030") String addressfrom, 
            @PathVariable @ApiParam(defaultValue = "0x3fc7e949eb925fb22d729cd035a7b48373901b96") String addressto, 
            @PathVariable @ApiParam(defaultValue = "0.0001") double amount,
            @PathVariable String password) throws Exception { 

        // Validate password
        Settings settings = new Settings();
        String true_password = settings.settings_password;
        System.out.println("True password: " + true_password + "Entered password: " + password);
        if (!true_password.equals(password))
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED);       
        
        // Send ETH
        Ethereum_web3j eth = new Ethereum_web3j();
        JSONObject result = eth.sendETH2ETH(amount, addressfrom, addressto);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }
    
    @RequestMapping(path="/Ethereum/Blockcypher/SendFromAddress/", method = RequestMethod.POST)
    @ApiOperation(value="Send ETH", tags="Ethereum API")
    public ResponseEntity<String> sendETC(
          @RequestParam(value="addressfrom", required=true) @ApiParam(defaultValue = "8b085c43ff55c042fb731abafa14865d6ca696fc") String addressfrom, 
          @RequestParam(value="addressto", required=true) @ApiParam(defaultValue = "383b3596d3d0aea8dde14c0c4462ffffd3c25a81") String addressto, 
          @RequestParam(value="amount", required=true) @ApiParam(defaultValue = "0.0001") double amount,
          @RequestParam(value="password", required=true) String password) throws Exception {      
    
        // Validate password
        Settings settings = new Settings();
        String true_password = settings.settings_password;
        System.out.println("True password: " + true_password + "Entered password: " + password);
        if (!true_password.equals(password))
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED); 
        
        Ethereum_Blockcypher eth = new Ethereum_Blockcypher();
        org.json.JSONObject result = eth.sendETH2ETH(addressfrom, addressto, amount);
 
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    } 
}