package com.bezalel.trading_api;

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

@CrossOrigin(allowCredentials = "true", origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RestController
@RequestMapping("/v1")
@Api(value = "Waves Testnet Endpoints")
public class Swagger_ExchangeSandbox {

    @RequestMapping(path="/Exchange/Testnet/GetPairQuote/{type}/{asset1}/{asset1_amount}/{asset2}", method = RequestMethod.GET)
    @ApiOperation(value="Get pair quote", tags="Exchange sandbox API")   
    public String getPairQuote(
            @PathVariable @ApiParam(defaultValue = "Buy") String type,
            @PathVariable @ApiParam(defaultValue = "BTC") String asset1,
            @PathVariable @ApiParam(defaultValue = "1.0") String asset1_amount,
            @PathVariable @ApiParam(defaultValue = "WAVES") String asset2) throws Exception {
        
        ExchangeSandbox exchange = new  ExchangeSandbox();
        double result = exchange.getPairQuote(asset1, Double.valueOf(asset1_amount), asset2, type);

        return String.valueOf(result); 
    }
  
    @RequestMapping(path="/Exchange/Testnet/Exchange_ETH2WAVES/{type}/{amount}/{address_base}/{address_quoted}/{fixedbaseamount}", method = RequestMethod.GET)
    @ApiOperation(value="Exchange assets", tags="Exchange sandbox API")   
    public ResponseEntity<String> ExchangePair(
            @PathVariable @ApiParam(defaultValue = "Sell") String type,
            @PathVariable @ApiParam(defaultValue = "0.00233") double amount, 
            @PathVariable @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String address_base, 
            @PathVariable @ApiParam(defaultValue = "3MqnoW5aY4x2eiwmM4ee1VjWwuJEvKnffj8") String address_quoted,           
            @PathVariable @ApiParam(defaultValue = "true") String fixedbaseamount) throws Exception {
        
        // Validate input 
        boolean fixed_base_amount = true;
        if (!fixedbaseamount.toLowerCase().equals("true") && !fixedbaseamount.toLowerCase().equals("false")) {
            return new ResponseEntity<>("Wrong input for fixedbaseamount: must true or false!", HttpStatus.BAD_REQUEST); 
        } else {
            if (fixedbaseamount.toLowerCase().equals("true")) 
                fixed_base_amount = true;
            else
                fixed_base_amount = false;
        }
        
        ExchangeSandbox exchange = new  ExchangeSandbox();
        String result = exchange.ExchangePair_ETH2WAVES(
            type, amount, address_base, address_quoted, fixed_base_amount);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(path="/Exchange/Testnet/GetFeeStructure/", method = RequestMethod.GET)
    @ApiOperation(value="Get current fees", tags="Exchange sandbox API")   
    public String GetFeeStructure() {
            
        ExchangeSandbox exchange = new  ExchangeSandbox();
        String fees = exchange.getFeeStructure();
            
        return fees; 
    }	
}
