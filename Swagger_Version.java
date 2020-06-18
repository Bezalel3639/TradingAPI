package com.bezalel.trading_api;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@CrossOrigin(allowCredentials = "true", origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
@RestController
@RequestMapping("/v1")
@Api(value = "Version Endpoints")
public class Swagger_Version {
    
    @Autowired
    BuildProperties buildProperties;    
   
    @RequestMapping(path="/Version/GetVersion", method = RequestMethod.GET) 
    @ApiOperation(value="Get trading API version", tags="VersionInfo")  
    public String getVersion() throws Exception {        
        String result = buildProperties.getVersion() + " Beta";
        return result;   
    }
    
    @RequestMapping(path="/Version/GetBuildTimestamp", method = RequestMethod.GET) 
    @ApiOperation(value="Get build timestamp", tags="VersionInfo")  
    public String getBuildTimestamp() throws Exception {    
        Instant instant = buildProperties.getTime(); 
        Date timestamp = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return formatter.format(timestamp);
    }
    
    @RequestMapping(path="/Version/GetComments", method = RequestMethod.GET) 
    @ApiOperation(value="Get release comments", tags="VersionInfo")  
    public String getComments() throws Exception {    
        String result = buildProperties.get("comment");        
        return result;    
    }
    
    @RequestMapping(path="/Version/GetJavaVersion", method = RequestMethod.GET) 
    @ApiOperation(value="Get Java version used for the build", tags="VersionInfo")  
    public String getJavaVersion() throws Exception {    
        String result = buildProperties.get("java.version");        
        return result;    
    }
}


