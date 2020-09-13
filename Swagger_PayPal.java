package com.bezalel.trading_api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Api(value = "PayPal Endpoints", description = "PayPal sandbox API")
public class Swagger_PayPal {

    @ApiOperation(value="Get balance for a currency", tags="PayPal sandbox API")     
    @RequestMapping(path="/PayPal/sandbox/GetBalance/{symbol}", method = RequestMethod.GET)    
    public ResponseEntity<String> GetBalance(
            @PathVariable @ApiParam(defaultValue = "USD") String symbol) throws Exception {
        
        String result = null;
        
        if (!symbol.equals("RUB") && !symbol.equals("EUR") && 
                !symbol.equals("HKD") && !symbol.equals("USD") && !symbol.equals("ILS"))
            return new ResponseEntity<>("The " + symbol + " currency is not supported!", HttpStatus.NOT_FOUND);
                
        PayPal pp = new PayPal();
        result = pp.paypal_getBalance(symbol);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } 
    }
    
    @ApiOperation(value="Get customer balance for a currency", tags="PayPal API")     
    @RequestMapping(path="/PayPal/GetCustomerBalance/{email}/{symbol}", method = RequestMethod.GET)    
    public ResponseEntity<String> GetCustomerBalance(
            @PathVariable @ApiParam(defaultValue = "bezalel4245@gmail.com") String email,
            @PathVariable @ApiParam(defaultValue = "RUB") String symbol) throws Exception {
        
        String result = null;
        
        if (!symbol.equals("RUB") && !symbol.equals("EUR") && 
                !symbol.equals("HKD") && !symbol.equals("USD") && !symbol.equals("ILS"))
            return new ResponseEntity<>("The " + symbol + " currency is not supported!", HttpStatus.NOT_FOUND);
                
        PayPal pp = new PayPal();
        result = pp.paypal_getCustomerBalance(email, symbol);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } 
    }
    
    @ApiOperation(value="Get customer sandbox balance for a currency", tags="PayPal sandbox API")     
    @RequestMapping(path="/PayPal/sandbox/GetCustomerBalance/{email}/{symbol}", method = RequestMethod.GET)    
    public ResponseEntity<String> GetCustomerSandboxBalance(
            @PathVariable @ApiParam(defaultValue = "sb-qrqup2693506@personal.example.com") String email,
            @PathVariable @ApiParam(defaultValue = "RUB") String symbol) throws Exception {
        
        String result = null;
        
        if (!symbol.equals("RUB") && !symbol.equals("EUR") && 
                !symbol.equals("HKD") && !symbol.equals("USD") && !symbol.equals("ILS"))
            return new ResponseEntity<>("The " + symbol + " currency is not supported!", HttpStatus.NOT_FOUND);
                
        PayPal pp = new PayPal();
        result = pp.paypal_getCustomerSandboxBalance(email, symbol);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } 
    }
    
    @ApiOperation(value="Make fiat deposit", tags="PayPal sandbox API")	   
    @RequestMapping(path="/PayPal/sandbox/Deposit/{email}/{amount}/{symbol}", method = RequestMethod.GET)
    public String Deposit(
            @PathVariable String email, 
            @PathVariable String amount, 
            @PathVariable String symbol) throws Exception {
         String result = "";
        
            PayPal pp = new PayPal();
        result = pp.paypal_Deposit(email, amount, symbol);

        return result; 
    } 
    
    @ApiOperation(value="Withdraw from an account", tags="PayPal sandbox API")    
    @RequestMapping(path="/PayPal/sandbox/Withdraw/", method = RequestMethod.POST)
    public ResponseEntity<String> Withdraw(
            @RequestParam(value="receiver", required=true) @ApiParam(defaultValue = "sb-sxwqb793030@personal.example.com") String receiver, 
            @RequestParam(value="amount", required=true) @ApiParam(defaultValue = "101.00") String amount, 
            @RequestParam(value="symbol", required=true) @ApiParam(defaultValue = "RUB") String symbol, 
            @RequestParam(value="accesstoken", required=true) @ApiParam(defaultValue = "A21AAHOKigllttuhG9jqUCbk2o22WMnwBUFrXEKBpajA9ZyAkTiu1swZrquP2rCjKNFHYDww6Hn7C7WxW1dlb_DBR2FCsxiJg") String accesstoken) throws Exception {
        
        String result = null;
        
        PayPal pp = new PayPal();
        result = pp.paypal_Withdraw(receiver, amount, symbol, accesstoken);
        
        if (result.equals("Token is invalid")) {
            return new ResponseEntity<>("Token is invalid!", HttpStatus.UNAUTHORIZED);
        }
        else if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
    } 
    
    @RequestMapping(path="/PayPal/sandbox/GetAccessToken/{password}", method = RequestMethod.POST) 
    @ApiOperation(value="Get operations token", tags="PayPal sandbox API")   
    public ResponseEntity<String> GetAccessToken(String password) throws Exception {        
            
        PayPal pp = new PayPal();
        String result = pp.utility_getAccessToken(password);
        
        if (result.equals("Wrong password!")) {
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED);
        }
        else if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    } 
    
    @RequestMapping(path="/PayPal/sandbox/Admin/VerifyAllWallets", method = RequestMethod.GET) 
    @ApiOperation(value="Verify all PayPay wallets of registered customers", tags="PayPal sandbox API")   
    public ResponseEntity<String> verifyAllPayPalWallets() throws Exception {        
            
        PayPal pp = new PayPal();
        boolean result = pp.verifyAllPayPalWallets();
        
        if (!result) {
            return new ResponseEntity<>("Errors were found!", HttpStatus.FOUND); 
        } else {
            return new ResponseEntity<>("No errors found!", HttpStatus.OK);
        }        
    } 
 }