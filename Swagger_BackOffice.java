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
@Api(value = "BackOffice Endpoints")
public class Swagger_BackOffice {

    @RequestMapping(path="/BackOffice/GetAssets/{user}", method = RequestMethod.GET)
    @ApiOperation(value="Get assets", tags="BackOffice API")   
    public ResponseEntity<String> getPairQuote(
            @PathVariable @ApiParam(defaultValue = "Pele") 
            String user) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.GetUserAssets(user);
        
        if (result.endsWith("404")) {
             return new ResponseEntity<>(HttpStatus.NOT_FOUND); // "no content" by default in response body
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
   }
       
    @RequestMapping(path="/BackOffice/AddAsset/{user}/{asset}/{address}", method = RequestMethod.POST)
    @ApiOperation(value="Add asset", tags="BackOffice API")   
    public ResponseEntity<String> AddAsset(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            String asset, 
            String address) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.AddAsset(user, asset, address);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/BackOffice/RemoveAsset/{user}/{asset}/{address}", method = RequestMethod.DELETE)
    @ApiOperation(value="Remove asset", tags="BackOffice API")   
    public ResponseEntity<String> RemoveAsset(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            String asset, 
            String address) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.RemoveAsset(user, asset, address);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
    
    @RequestMapping(path="/BackOffice/ModifyAssetData/{user}/{index}", method = RequestMethod.PATCH)
    @ApiOperation(value="Remove asset", tags="BackOffice API")   
    public ResponseEntity<String> ModifyAssetData(
            @PathVariable @ApiParam(defaultValue = "Ronaldo") String user, 
            int index, 
            String newdata) throws Exception {
        
        BackOffice bo = new BackOffice();
        String result = bo.UpdateAssetData(user, index, newdata);
        
        if (result.endsWith("404")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }        
    }
}
