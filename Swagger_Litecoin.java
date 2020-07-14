package com.bezalel.trading_api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigInteger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1")
@Api(value = "Litecoin Endpoints", description = "Litecoin API") 
public class Swagger_Litecoin {

    @RequestMapping(path="/LTC/LastBlock/", method = RequestMethod.GET)
    @ApiOperation(value="Get last block", tags="Litecoin API")
    public long getLastBlock() throws Exception {
        long result = 0;
        
        Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher();
        result = litecoin.getLastBlock();
 
        return result; 
    }
    
    @RequestMapping(path="/LTC/IsAddressValid/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Check if address is valid", tags="Litecoin API")
    public boolean IsAddressValid(@PathVariable String address) throws Exception {  
        Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher(); 
        return litecoin.validateAddress(address); 
    }
    
    @RequestMapping(path="/LTC/GetNewAddress/{email}/{notes}", method = RequestMethod.GET)
    @ApiOperation(value="Create new LTC address", tags="Litecoin API")
    public ResponseEntity<String> getNewAddress(   
            @PathVariable String email,
            @PathVariable String notes) throws Exception {   	
            
        Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher();
        org.json.JSONObject result  = litecoin.getNewAddress(email, notes);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }
     
    @RequestMapping(path="/LTC/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Litecoin API")
    public long getBalance(@PathVariable String address) throws Exception {  
        
        long result = 0;    	
            
        Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher();
        result = litecoin.getBalance(address);
 
        return result; 
    }
   
    @RequestMapping(path="/LTC/LTCRate/", method = RequestMethod.GET)
    @ApiOperation(value="Get LTC rate in USD", tags="Litecoin API")
    public double getLTCRate() throws Exception {
        double result = 0;
         
        Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher();
        result = litecoin.getLTCRate();
  
        return result; 
    }
    
    @RequestMapping(path="/LTC/SendFromAddress/{addressfrom}/{addressto}/{amount}/{password}", method = RequestMethod.POST)
    @ApiOperation(value="Send LTC", tags="Litecoin API")
    public ResponseEntity<String> sendLTC(
            @PathVariable @ApiParam(defaultValue = "LPR3HtYwfguAtUnzn6vYPirD6G9HHdnLz9") String addressfrom, 
            @PathVariable @ApiParam(defaultValue = "LVgyPg5DyXhHF3VwwRy6VnmFdBuVf39LKL") String addressto, 
            @PathVariable @ApiParam(defaultValue = "0.00001") double amount,
            @PathVariable String password) throws Exception {  
    
        // Validate password
        Settings settings = new Settings();
        String true_password = settings.settings_password;
        System.out.println("True password: " + true_password + "Entered password: " + password);
        if (!true_password.equals(password))
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED); 
        
        Litecoin_Blockcypher litecoin = new Litecoin_Blockcypher();
        org.json.JSONObject result = litecoin.SendLTC2LTC(addressfrom, addressto, amount);
 
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    } 
}