package com.bezalel.trading_api;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

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
@Api(value = "BackOffice Endpoints")
public class Swagger_BackOffice {
    
    @RequestMapping(path="/BackOffice/Login/ValidateUser/{email}/{password}", method = RequestMethod.GET)
    @ApiOperation(value="Validate user", tags="BackOffice API")    
    public ResponseEntity<String> validateUser(
            @PathVariable @ApiParam(defaultValue = "malevany@gmail.com") String email,
            String password) throws Exception {

        BackOffice bo = new BackOffice();
        BackOffice.UserValidation result = bo.new UserValidation(); 
        result = bo.validateUser(email, password); 
         
        if (result.code.equals("200")) {
             return new ResponseEntity<>("A record was found in the DB! Admin status: " + result.isadmin, HttpStatus.OK);
        } else if (result.message.equals("Email not was found in DB")) 
            return new ResponseEntity<>("Email not was found in DB", HttpStatus.NOT_FOUND);          
        else if (result.message.equals("Email was found in the DB, but password is wrong!")) {
            return new ResponseEntity<>("Email was found in the DB, but password is wrong!", HttpStatus.NOT_FOUND);
        }  else {  
            return new ResponseEntity<>("Unrecongnized error", HttpStatus.UNAUTHORIZED);  
        }
    }
    
    @RequestMapping(path="/BackOffice/Login/ValidateSandboxUser/{email}/{password}", method = RequestMethod.GET)
    @ApiOperation(value="Validate sandbox user", tags="BackOffice API")    
    public ResponseEntity<String> validateSandboxUser(
            @PathVariable @ApiParam(defaultValue = "malevany@gmail.com") String email,
            String password) throws Exception {

        BackOffice bo = new BackOffice();
        BackOffice.UserValidation result = bo.new UserValidation(); 
        result = bo.validateSandboxUser(email, password); 
         
        if (result.code.equals("200")) {
             return new ResponseEntity<>("A record was found in the DB! Admin status: " + result.isadmin, HttpStatus.OK);
        } else if (result.message.equals("Email not was found in DB")) 
            return new ResponseEntity<>("Email not was found in DB", HttpStatus.NOT_FOUND);          
        else if (result.message.equals("Email was found in the DB, but password is wrong!")) {
            return new ResponseEntity<>("Email was found in the DB, but password is wrong!", HttpStatus.NOT_FOUND);
        }  else {  
            return new ResponseEntity<>("Unrecongnized error", HttpStatus.UNAUTHORIZED);  
        }
    }
    
    @RequestMapping(path="/BackOffice/Login/RegisterUser/", method = RequestMethod.POST)
    @ApiOperation(value="Register user", tags="BackOffice API")
    public ResponseEntity<String> registerUser(            
            @RequestParam(value="email", required=true) @ApiParam(defaultValue = "bezalel5390@yandex.ru") String email,
            @RequestParam(value="password", required=true) String password) throws Exception {
          
        BackOffice bo = new BackOffice();
        String result = bo.registerUser(email, password, "");      
         
        if (result.equals("200")) {      
            return new ResponseEntity<>("A user account was created successfully!", HttpStatus.OK);        
        } else if (result.equals("The user already exists!")) {
            return new ResponseEntity<>("The user already exists!", HttpStatus.OK);
        }  else if (result.equals("Failure while generating address!")) {
            return new ResponseEntity<>("Failure while generating address!", HttpStatus.NOT_FOUND);
        }  else {  
            return new ResponseEntity<>("Unrecongnized error", HttpStatus.UNAUTHORIZED);  
        }
    }
        
    @RequestMapping(path="/BackOffice/Login/RegisterSandboxUser/", method = RequestMethod.POST)
    @ApiOperation(value="Register sandbox user", tags="BackOffice API")
    public ResponseEntity<String> registerSandboxUser(            
            @RequestParam(value="email", required=true) @ApiParam(defaultValue = "bezalel5390@yandex.ru") String email,
            @RequestParam(value="password", required=true) String password) throws Exception {
          
        BackOffice bo = new BackOffice();
        String result = bo.registerSandboxUser(email, password, "");      
         
        if (result.equals("200")) {      
            return new ResponseEntity<>("A user account was created successfully!", HttpStatus.OK);        
        } else if (result.equals("The user already exists!")) {
            return new ResponseEntity<>("The user already exists!", HttpStatus.BAD_REQUEST);
        }  else if (result.equals("Failure while generating address!")) {
            return new ResponseEntity<>("Failure while generating address!", HttpStatus.NOT_FOUND);
        }  else {  
            return new ResponseEntity<>("Unrecongnized error", HttpStatus.UNAUTHORIZED);  
        }
    }
        
    @RequestMapping(path="/BackOffice/Login/RegisterSandboxUserWithScreenname/", method = RequestMethod.POST)
    @ApiOperation(value="Register user with screename", tags="BackOffice API")
    public ResponseEntity<String> registerSandboxUserWithScreenName(
            @RequestParam(value="email", required=true) @ApiParam(defaultValue = "bezalel5390@yandex.ru") String email,
            @RequestParam(value="password", required=true) String password,
            @RequestParam(value="screenName", required=false) String screenname) throws Exception {
         
        BackOffice bo = new BackOffice();
        String result = bo.registerSandboxUser(email, password, screenname);      
         
        if (result.equals("200")) {      
            return new ResponseEntity<>("A user account was created successfully!", HttpStatus.OK);        
        } else if (result.equals("The user already exists!")) {
            return new ResponseEntity<>("The user already exists!", HttpStatus.BAD_REQUEST);          
          }  else {  
            return new ResponseEntity<>("Unrecongnized error", HttpStatus.UNAUTHORIZED);  
        }
    }
    
    @RequestMapping(path="/BackOffice/GetAssets/{user}", method = RequestMethod.GET)
    @ApiOperation(value="Get assets", tags="BackOffice API")   
    public ResponseEntity<String> getAssets(
            @PathVariable @ApiParam(defaultValue = "Pele") 
            String user) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.getUserAssets(user);
        
        if (result.endsWith("404")) {
             return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
       
    @RequestMapping(path="/BackOffice/AddPortfolioAsset/{user}/{asset}/{address}", method = RequestMethod.POST)
    @ApiOperation(value="Add portfolio asset", tags="BackOffice API")   
    public ResponseEntity<String> addPortfolioAsset(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            String asset, 
            String address) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.addPortfolioAsset(user, asset, address);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/BackOffice/AddBalanceAsset/{user}/{asset}/{address}", method = RequestMethod.POST)
    @ApiOperation(value="Add balance asset", tags="BackOffice API")   
    public ResponseEntity<String> addBalanceAsset(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            String asset, 
            String address) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.addBalanceAsset(user, asset, address);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/BackOffice/RemoveAsset/{user}/{asset}/{address}", method = RequestMethod.DELETE)
    @ApiOperation(value="Remove asset", tags="BackOffice API")   
    public ResponseEntity<String> removeAsset(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            String asset, 
            String address) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.removeAsset(user, asset, address);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }        
    
    @RequestMapping(path="/BackOffice/ModifyAssetData/{user}/{index}", method = RequestMethod.PATCH)
    @ApiOperation(value="Modify asset", tags="BackOffice API")   
    public ResponseEntity<String> modifyAssetData(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            int index, 
            String newdata) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.updateAssetData(user, index, newdata);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/BackOffice/GetBalancesAssets/{user}", method = RequestMethod.GET)
    @ApiOperation(value="Get assets for balances", tags="BackOffice API")   
    public ResponseEntity<String> getBalancesAssets(
            @PathVariable @ApiParam(defaultValue = "user9@yandex.ru") 
            String user) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.getUserBalanceAssets(user);
        
        if (result.endsWith("404")) {
             // Reserved for connection failure
             return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (result.equals("The user is not found!")) {
                return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }    
    
    @RequestMapping(path="/BackOffice/GetSandboxBalancesAssets/{user}", method = RequestMethod.GET)
    @ApiOperation(value="Get assets for sandbox balances", tags="BackOffice API")   
    public ResponseEntity<String> getSandboxBalancesAssets(
            @PathVariable @ApiParam(defaultValue = "Pele") 
            String user) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.getUserSandBoxBalanceAssets(user);
        
        if (result.endsWith("404")) {
             // Reserved for connection failure
             return new ResponseEntity<>(HttpStatus.NOT_FOUND); // "no content" by default in response body if result omitted
        } else if (result.equals("The user is not found!")) {
                return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
   }
}
