package com.bezalel.trading_api;

import java.math.BigInteger;

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

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1")
@Api(value = "Ethereum Endpoints", description = "Ethereum sandbox API")
public class Swagger_EthereumTestnet {
	
    @RequestMapping(path="/Ethereum/Testnet/LastBlock/", method = RequestMethod.GET)
    @ApiOperation(value="Get last block", tags="Ethereum sandbox API")
	public BigInteger getLastBlock() {
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
        BigInteger result = eth.getLastBlock();
          
        return result; 
    }
    
    @RequestMapping(path="/Ethereum/Testnet/Balance/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Get balance", tags="Ethereum sandbox API")
    public BigInteger getBalance(
            @PathVariable 
            @ApiParam(defaultValue = "0x0d6f2055eedae95ccd888661aa5e3c05c1aeb64d") String address) 
            throws Exception {
    
    	EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
     	BigInteger result = eth.getBalance(address);
   
     	return result; 
    }

    @RequestMapping(path="/Ethereum/Testnet/IsAddressValid/{address}", method = RequestMethod.GET)
    @ApiOperation(value="Check if address is valid", tags="Ethereum sandbox API")
    public boolean IsAddressValid(
            @PathVariable 
            @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String address) throws Exception {

        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
        return eth.IsValidAddress(address); 
    }
    
    @RequestMapping(path="/Ethereum/Testnet/GetNewAddress/{user}/{password}", method = RequestMethod.GET)
    @ApiOperation(value="Generate new Ethreum testnet address", tags="Ethereum sandbox API")
    public ResponseEntity<String> GetNewAddress(
            @PathVariable  @ApiParam(defaultValue = "Ronaldo") String user, 
            @PathVariable 
            @ApiParam(defaultValue = "37*****") String password) throws Exception {

        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
        String result = eth.GetNewAddress(user, password); 
        
        if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }      
    }
}
