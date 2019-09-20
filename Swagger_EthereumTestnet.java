package com.bezalel.trading_api;

import java.math.BigInteger;

import org.json.JSONObject;
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
            @ApiParam(defaultValue = "**3737*****") String password) throws Exception {

        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
        String result = eth.GetNewAddress(user, password); 
        
        if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); 
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }      
    }
    
    @RequestMapping(path="/Ethereum/Testnet/Send/{walletfrom}/{addressto}/{amount}/{password}", method = RequestMethod.POST)
    @ApiOperation(value="Send test ETH", tags="Ethereum sandbox API")
    public ResponseEntity<String> sendETHfromWallet(
        @PathVariable String walletfrom, 
        @PathVariable @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String addressto, 
        @PathVariable double amount,
        @PathVariable String password) throws Exception { 
        
        // Validate wallet name
        Settings settings = new Settings();
        JSONObject wallet =  settings.locateETHWallet(walletfrom);
        if (wallet == null)
            return new ResponseEntity<>("The wallet " + walletfrom + " was not found", 
                HttpStatus.NOT_FOUND);
        
        // Validate password
        if (!wallet.getString("password").equals(password))
            return new ResponseEntity<>("The password is not valid", HttpStatus.FORBIDDEN);
       
        // Get full file name for the wallet
        String walletfile = null;
        String OS = System.getProperty("os.name");
        if (OS.equals("Windows 10")) {
            walletfile = settings.win10_devfolder + "\\" + wallet.getString("wallet");
        }
        else if (OS.equals("Linux")) {
            walletfile = settings.linux_store + "/" + wallet.getString("wallet");
        }               
        
        // Send ETH
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();  
        String result = eth.SendETH(walletfile, password, addressto, amount); 
        
        // Validate results
        if (result.endsWith("404")) {
            return new ResponseEntity<>("The wallet not found: " + walletfrom, HttpStatus.NOT_FOUND); 
        } else if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }  
    } 
    
    @RequestMapping(path="/Ethereum/Testnet/SendFromAddress/{addressfrom}/{addressto}/{amount}/{password}", method = RequestMethod.POST)
    @ApiOperation(value="Send test ETH", tags="Ethereum sandbox API")
    public ResponseEntity<String> sendETHfromAddress(
        @PathVariable @ApiParam(defaultValue = "0x0d6f2055eedae95ccd888661aa5e3c05c1aeb64d") String addressfrom, 
        @PathVariable @ApiParam(defaultValue = "0x8d8057d0810996077effe2283ef5788178a91e61") String addressto, 
        @PathVariable @ApiParam(defaultValue = "0.02") double amount,
        @PathVariable String password) throws Exception { 
        
        EthereumTestnet_web3j eth = new EthereumTestnet_web3j();
        
        // Validate addresses
        if (!eth.IsValidAddress(addressfrom))
            return new ResponseEntity<>("The source address does not exist", HttpStatus.NOT_FOUND);
        if (!eth.IsValidAddress(addressto))
            return new ResponseEntity<>("The destination address does not exist", HttpStatus.NOT_FOUND);
         
        // Validate existence of wallet for the source address 
        Settings settings = new Settings();
        JSONObject wallet =  settings.locateETHWalletByAddress(addressfrom);
        if (wallet == null)
            return new ResponseEntity<>("The wallet for the address " + addressfrom + " was not found", 
                HttpStatus.NOT_FOUND);
        
        // Validate password
        if (!wallet.getString("password").equals(password))
            return new ResponseEntity<>("The password is not valid", HttpStatus.FORBIDDEN);
       
        // Get full file name for the wallet
        String walletfile = null;
        String OS = System.getProperty("os.name");
        if (OS.equals("Windows 10")) {
            walletfile = settings.win10_devfolder + "\\" + wallet.getString("wallet");
        }
        else if (OS.equals("Linux")) {
            walletfile = settings.linux_store + "/" + wallet.getString("wallet");
        }
        
        if (walletfile == null)
            return new ResponseEntity<>("The wallet not found", HttpStatus.NOT_FOUND);
       
        // Send ETH
        String result = eth.SendETH(walletfile, password, addressto, amount); //Double.valueOf(amount)
        
        // Validate results
        if (result.endsWith("404")) {
            return new ResponseEntity<>("The wallet not found: " + walletfile, HttpStatus.NOT_FOUND); 
        } else if (result.endsWith("403")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }  
    }
}
