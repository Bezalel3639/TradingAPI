package com.bezalel.trading_api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@CrossOrigin(allowCredentials = "true", origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RestController
@RequestMapping("/v1")
@Api(value = "Yandex Endpoints")
public class Swagger_Yandex {
    
    @RequestMapping(path="/Yandex/GetTemporaryToken/{client_id}/{redirect_uri}/{scope}", method = RequestMethod.GET) 
    @ApiOperation(value="Get temporaty token from Yandex", tags="Yandex API")   
    public ResponseEntity<String> getYandexTemporayToken(
            //@PathVariable @ApiParam(defaultValue = "2724A3E4816E3DA8923A1B6D342388D4E324E6F51F434FA9D8112BC0000A1B24") String client_id,
            //@PathVariable @ApiParam(defaultValue = "http://48bd3965.ngrok.io/TradingAPI/Yandex_Servlet") String redirect_uri,
            String client_id, 
            String redirect_uri,
            @PathVariable @ApiParam(defaultValue = "account-info") String scope) throws Exception {
        
        Yandex gateway = new Yandex();
        String result = gateway.yandex_getTemporaryToken(client_id, redirect_uri, scope);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/Yandex/GetInstanceID/{client_id}", method = RequestMethod.GET) 
    @ApiOperation(value="Get instance_id from Yandex", tags="Yandex API")   
    public ResponseEntity<String> getYandexInstanceId(String client_id) throws Exception {
        
        Yandex gateway = new Yandex();
        String result = gateway.yandex_getInstanceId(client_id);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Wrong input!", HttpStatus.BAD_REQUEST); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/Yandex/DepositWithCard/{amount}/{emailfrom}/{gethtmlform}", method = RequestMethod.GET) 
    @ApiOperation(value="Get orderId from Yandex to deposit with card", tags="Yandex API")   
    public ResponseEntity<String> DepositWithCard_GetHtmlForm(
            @PathVariable @ApiParam(defaultValue = "2.00") String amount,
            @PathVariable @ApiParam(defaultValue = "NA") String emailfrom,
            @PathVariable @ApiParam(defaultValue = "false") boolean gethtmlform) throws Exception {
        
        Yandex gateway = new Yandex();
        String result = gateway.yandex_requestPaymentFromCard(amount, emailfrom, gethtmlform);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } 
    }

    @RequestMapping(path="/Yandex/Withdraw2Account/", method = RequestMethod.POST) 
    @ApiOperation(value="Withdraw to Yandex account identified by email", tags="Yandex API")   
    public ResponseEntity<String> TransferWithP2P(
            @RequestParam(value="payee_email", required=true) @ApiParam(defaultValue = "bezalel5390@yandex.ru") String payee_email,
            @RequestParam(value="amount", required=true) @ApiParam(defaultValue = "3.00") String amount,
            @RequestParam(value="message", required=true) String message,
            @RequestParam(value="password", required=true) String password) throws Exception {
        
        String result = null;
            
        Yandex gateway = new  Yandex();
        result = gateway.yandex_requestP2PTransfer(payee_email, amount, message, password);
        
        if (result.equals("Wrong password!")) {
            return new ResponseEntity<>("Wrong password!", HttpStatus.UNAUTHORIZED);
        }
        else if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    } 
    
    @RequestMapping(path="/Yandex/GetTransaction/{id}", method = RequestMethod.GET) 
    @ApiOperation(value="Get transaction details from Yandex", tags="Yandex API")   
    public ResponseEntity<String> getYandexTX(
            @PathVariable @ApiParam(defaultValue = "628783589615016012") String id) throws Exception {
        
        Yandex gateway = new Yandex();
        String result = gateway.yandex_getOperationDetails(id);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/Yandex/GetHistory/{type}/{records}/{start_record}/{show_details}", method = RequestMethod.GET) 
    @ApiOperation(value="Get operations history from Yandex", tags="Yandex API")   
    public ResponseEntity<String> getYandexHistory(
            @PathVariable @ApiParam(defaultValue = "1") String records, 
            @PathVariable @ApiParam(defaultValue = "true") boolean show_details) throws Exception {
        
        // Note: type, start_record are not used in this version 
        String type = null;
        String start_record = null;
        Yandex gateway = new Yandex();
        String result = gateway.yandex_getOperationsHistory(type, records, start_record, show_details);
        
        if (result.equals("-1")) {
            return new ResponseEntity<>("Something went wrong!", HttpStatus.UNAUTHORIZED); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }       
}