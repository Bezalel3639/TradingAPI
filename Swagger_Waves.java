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

@CrossOrigin(allowCredentials = "true", origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RestController
@RequestMapping("/v1")
@Api(value = "Waves Endpoints", description = "Waves API")
public class Swagger_Waves {

    @RequestMapping(path="/Waves/LastBlock/", method = RequestMethod.GET)
    @ApiOperation(value="Get last block", tags="Waves API")   
    public int getLastBlock() throws Exception {
        
         Waves waves = new  Waves();
        int result = waves.getLastBlock();

        return result; 
    }	
    
    @RequestMapping(path="/Waves/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Waves API")   
    public long getBalance(@PathVariable @ApiParam(defaultValue = "3PCSuxn6F4pnLe4X9THYPopWhaqh86jJB6B") String address) throws Exception { 
        
        Waves waves = new  Waves();
        long result = waves.getBalance(address);
 
        return result; 
    }
    
    @RequestMapping(path="/Waves/IsAddressValid/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Check if address is valid", tags="Waves API")
    public boolean IsAddressValid(
            @PathVariable String address) throws Exception {   
        Waves waves = new  Waves();        
        return waves.validateAddress(address); 
    }
    
    @RequestMapping(path="/Waves/WavesRate/", method = RequestMethod.GET)
      @ApiOperation(value="Get Waves rate in USD", tags="Waves API")
    public double getWavesRate() throws Exception {    	
        Waves waves = new  Waves();
          double result = waves.getWavesRate(); 
          return result;    
    } 
    
    @RequestMapping(path="/Waves/GetNewAddress/{email}/{notes}", method = RequestMethod.GET)
    @ApiOperation(value="Generate new WAVES address", tags="Waves API")
    public ResponseEntity<String> getNewAddress(
            @PathVariable String email,
            @PathVariable String notes) throws Exception {

        Waves waves = new  Waves();  
        JSONObject result = waves.getNewAddress(email, notes);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }
    
    @RequestMapping(path="/Waves/SendFromAddress/", method = RequestMethod.POST)
    @ApiOperation(value="Send WAVES", tags="Waves API")
    public ResponseEntity<String> sendWAVESfromAddress(
            @RequestParam(value="addressfrom", required=true) @ApiParam(defaultValue = "3PNKLfFSYFyXHRiqMHz1wVwHDiVBVey5viL") String addressfrom, 
            @RequestParam(value="addressto", required=true) @ApiParam(defaultValue = "3PDKC26V2sdxGtvnqmkgTwdwT6zeSB72UqK") String addressto, 
            @RequestParam(value="amount", required=true) @ApiParam(defaultValue = "0.001") double amount,
            @RequestParam(value="password", required=true) String password) throws Exception { 

        // Validate password
        Settings settings = new Settings();
        String true_password = settings.settings_password;
        System.out.println("True password: " + true_password + "Entered password: " + password);
        if (!true_password.equals(password))
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED);       
        
        // Send WAVES
        Waves waves = new Waves();
        JSONObject result = waves.sendWAVES2WAVES(amount, addressfrom, addressto);
        
        // Validate results
        if (result.get("status").equals("-1")) {          
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result.get("data").toString(), HttpStatus.OK);  
        }
    }
}