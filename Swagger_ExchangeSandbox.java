package com.bezalel.trading_api;

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
@Api(value = "Waves Testnet Endpoints", description = "Exchange sandbox API")
public class Swagger_ExchangeSandbox {

    @RequestMapping(path="/Exchange/Testnet/getPairQuote/{type}/{asset1}/{asset1_amount}/{asset2}", method = RequestMethod.GET)
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
    
    @RequestMapping(path="/Exchange/Testnet/Exchange/{type}/{asset1}/{asset1_amount}/{address1}/{asset2}/{asset2_amount}/{address2}", method = RequestMethod.GET)
    @ApiOperation(value="Exchange assets", tags="Exchange sandbox API")   
    public String ExchangePair(
            @PathVariable @ApiParam(defaultValue = "Sell") String type,
            @PathVariable @ApiParam(defaultValue = "BTC") String asset1,
            @PathVariable @ApiParam(defaultValue = "1") String asset1_amount, // not used in scenario 1
            @PathVariable @ApiParam(defaultValue = "2N6V12gcR2XaFGFMGK7qeNTjqcxo3wAzWdp") String address1, 
            @PathVariable @ApiParam(defaultValue = "WAVES") String asset2,
            @PathVariable @ApiParam(defaultValue = "0.00233") String asset2_amount,
            @PathVariable @ApiParam(defaultValue = "3MqnoW5aY4x2eiwmM4ee1VjWwuJEvKnffj8") String address2) throws Exception {
        
        ExchangeSandbox exchange = new  ExchangeSandbox();
        double amount1 = Double.valueOf(asset1_amount);
        double amount2 = Double.valueOf(asset2_amount);
        String result = exchange.ExchangePair(type, asset1, amount1, address1, asset2, amount2, address2);

        return result; 
    }	
    
    @RequestMapping(path="/Exchange/Testnet/GetFeeStructure/", method = RequestMethod.GET)
    @ApiOperation(value="Get current fees", tags="Exchange sandbox API")   
    public String GetFeeStructure() {
            
        ExchangeSandbox exchange = new  ExchangeSandbox();
        String fees = exchange.getFeeStructure();
            
        return fees; 
    }	
}
