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
    
    @RequestMapping(path="/FiatSolutions/GetProvider2Balance/", method = RequestMethod.GET) 
    @ApiOperation(value="Get RUR balance from Yandex", tags="FiatSolutions API")   
    public ResponseEntity<Double> getYandexBalance() throws Exception {
        
        FiatSolutions gateway = new FiatSolutions();
        Double result = gateway.yandex_getBalance();
        
        if (result.equals(-1)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/FiatSolutions/GetProvider2/History/{type}/{records}/{start_record}", method = RequestMethod.GET) 
    @ApiOperation(value="Get operations history from Yandex", tags="FiatSolutions API")   
    public ResponseEntity<String> getYandexHistory(String records) throws Exception {
        
        // Note: type, start_record are not used in this version 
        String type = null;
        String start_record = null;
        FiatSolutions gateway = new FiatSolutions();
        String result = gateway.yandex_getOperationsHistory(type, records, start_record);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    } 
    
    @RequestMapping(path="/FiatSolutions/GetProvider2/Transaction/{id}", method = RequestMethod.GET) 
    @ApiOperation(value="Get transaction details from Yandex", tags="FiatSolutions API")   
    public ResponseEntity<String> getYandexTX(
            @PathVariable @ApiParam(defaultValue = "628783589615016012") String id) throws Exception {
        
        FiatSolutions gateway = new FiatSolutions();
        String result = gateway.yandex_getOperationDetails(id);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    } 
    
    @RequestMapping(path="/FiatSolutions/GetProvider2InstanceID/{client_id}", method = RequestMethod.GET) 
    @ApiOperation(value="Get instance_id from Yandex", tags="FiatSolutions API")   
    public ResponseEntity<String> getYandexInstanceId(String client_id) throws Exception {
        
        FiatSolutions gateway = new FiatSolutions();
        String result = gateway.yandex_getInstanceId(client_id);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Wrong input!", HttpStatus.BAD_REQUEST); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/FiatSolutions/GetProvider2/GetTemporaryToken/{client_id}/{redirect_uri}/{scope}", method = RequestMethod.GET) 
    @ApiOperation(value="Get temporaty token from Yandex", tags="FiatSolutions API")   
    public ResponseEntity<String> getYandexTemporayToken(
            String client_id, 
            String redirect_uri,
            @PathVariable @ApiParam(defaultValue = "account-info") String scope) throws Exception {
        
        FiatSolutions gateway = new FiatSolutions();
        String result = gateway.yandex_getTemporaryToken(client_id, redirect_uri, scope);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/FiatSolutions/GetProvider2/TransferWithP2P/{payee_account}/{amount}/{message}/{password}", method = RequestMethod.GET) 
    @ApiOperation(value="P2P transfer with Yandex", tags="FiatSolutions API")   
    public ResponseEntity<String> TransferWithP2P(
            @PathVariable @ApiParam(defaultValue = "4100111080768575") String payee_account,
            @PathVariable @ApiParam(defaultValue = "3.45") String amount,
            String message,
            String password) throws Exception {        
            
        FiatSolutions gateway = new FiatSolutions();
        String result = gateway.yandex_requestP2PTransfer(payee_account, amount, message, password);
        
        if (result.equals("Wrong password!")) {
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED);
        }
        else if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    } 
    
    @RequestMapping(path="/FiatSolutions/GetProvider2/DepositWithCard_GetHtmlForm/{amount}", method = RequestMethod.GET) 
    @ApiOperation(value="Get form from Yandex to deposit with card", tags="FiatSolutions API")   
    public ResponseEntity<String> DepositWithCard_GetHtmlForm(
            @PathVariable @ApiParam(defaultValue = "8.00") String amount) throws Exception {
        
        FiatSolutions gateway = new FiatSolutions();
        String result = gateway.yandex_requestPaymentFromCard(amount);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/FiatSolutions/GetProvider2/DepositWithCard_ProcessPayment", method = RequestMethod.GET) 
    @ApiOperation(value="Process deposit from card", tags="FiatSolutions API")   
    public void DepositWithCard_ProcessPayment() throws Exception {        
        FiatSolutions gateway = new FiatSolutions();
        gateway.utility_createCardInputForm();
    } 
    
    @RequestMapping(path="/FiatSolutions/GetRate/{symbol}/{base}", method = RequestMethod.GET) 
    @ApiOperation(value="Get currency rate", tags="FiatSolutions API")   
    public ResponseEntity<Double> getRate(
            @PathVariable String symbol, 
            @PathVariable @ApiParam(defaultValue = "USD") String base) throws Exception {
        
        // Validate symbol
        if (symbol.length() != 3)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // invalid symbol
        
        FiatSolutions gateway = new FiatSolutions();
        Double result = gateway.fiatGetRate(symbol, base);      
         
        return new ResponseEntity<>(result, HttpStatus.OK);
   } 
}