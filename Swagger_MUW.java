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
@Api(value = "MUW Endpoints")
public class Swagger_MUW {   
         
    @RequestMapping(path="/MUW/GetBalance/{email}/{symbol}", method = RequestMethod.GET) 
    @ApiOperation(value="Get customer balance for a currency", tags="MUW API")
    public ResponseEntity<String> GetCustomerBalance(
            @PathVariable @ApiParam(defaultValue = "bezalel5390@yandex.ru") String email,
            @PathVariable @ApiParam(defaultValue = "RUB") String symbol) throws Exception {
        
        String result = null;
        
        if (!symbol.equals("RUB") && !symbol.equals("EUR") && 
                !symbol.equals("HKD") && !symbol.equals("USD") && !symbol.equals("ILS"))
            return new ResponseEntity<>("The " + symbol + " currency is not supported!", HttpStatus.NOT_FOUND);
                
        MUW pp = new MUW();
        result = pp.getCustomerBalance(email, symbol);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } 
    }
    
    @RequestMapping(path="/MUW/GetHeapBalance/", method = RequestMethod.GET) 
    @ApiOperation(value="Get RUR heap balance from Yandex", tags="MUW API")   
    public ResponseEntity<Double> getYandexHeapBalance() throws Exception {
              
        MUW gateway = new MUW();
        Double result = gateway.yandex_getBalance();
          
        if (result.equals(-1)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }     
    
    @RequestMapping(path="/MUW/VerifyAllWallets", method = RequestMethod.GET) 
    @ApiOperation(value="Verify all MUW fiat wallets of registered customers", tags="MUW API")   
    public ResponseEntity<String> verifyAllPayPalWallets() throws Exception {        
            
        MUW muw = new MUW();
        boolean result = muw.verifyAllWallets();
        
        if (!result) {
            return new ResponseEntity<>("Errors were found!", HttpStatus.FOUND); 
        } else {
            return new ResponseEntity<>("No errors found!", HttpStatus.OK);
        }        
    } 
}