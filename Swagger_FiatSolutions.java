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
@Api(value = "FiatSolutions Endpoints")
public class Swagger_FiatSolutions {    
    
    @RequestMapping(path="/FiatSolutions/GetRate/{symbol}/{base}", method = RequestMethod.GET) 
    @ApiOperation(value="Get currency rate", tags="FiatSolutions API")   
    public ResponseEntity<Double> getRate(
            @PathVariable String symbol, 
            @PathVariable @ApiParam(defaultValue = "USD") String base) throws Exception {
        
        // Validate symbol
        if (symbol.length() != 3)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        
        FiatSolutions gateway = new FiatSolutions();
        Double result = gateway.fiatGetRate(symbol, base);      
         
        return new ResponseEntity<>(result, HttpStatus.OK);
   }
    
    @RequestMapping(path="/FiatSolutions/GetProvider1Balance/{email}/{symbol}", method = RequestMethod.GET) 
    @ApiOperation(value="Get fiat balance", tags="FiatSolutions API")   
    public ResponseEntity<String> getBalance(
            @PathVariable String email, 
            @PathVariable @ApiParam(defaultValue = "USD") String symbol) throws Exception {
        
        ePay gateway = new ePay();
        String result = gateway.getBalance(email, symbol);
        
        if (result.equals("Account does not exist")) {
             return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }  
}