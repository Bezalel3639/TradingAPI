package com.bezalel.trading_api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class TradingApiApplication extends SpringBootServletInitializer {
    
    @Autowired
    BuildProperties buildProperties;

    public static void main(String[] args) {
    	SpringApplication.run(TradingApiApplication.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)  {
    	return builder.sources(TradingApiApplication.class);
    }
    
    @EventListener(ApplicationReadyEvent.class) 
    public void runStartupTasks() {
        System.out.println("RunStartupTasks> " + buildProperties.getVersion()); 
       
        String OS = System.getProperty("os.name");  
        if (OS.equals("Windows 10")) {
            System.out.println("RunStartupTasks> This is Windows!");            
        } else if (OS.equals("Linux")) {
            System.out.println("RunStartupTasks> This is Linux!");
        }
        
        try {
            JSONObject file_settings = utility_ReadServerSettings();
            if (file_settings.get("status").equals("-1")) {          
                System.out.println("RunStartupTasks> " + file_settings.get("message").toString());
            } else {
                // Setup: IP
                System.out.println("RunStartupTasks> mongodb IP: " + file_settings.get("mongodb"));
                Settings.mongodb_host = file_settings.get("mongodb").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public JSONObject utility_ReadServerSettings() throws Exception {
        String ffn = null;
        JSONObject settings = new JSONObject();
        
        String OS = System.getProperty("os.name");        
        if (OS.equals("Windows 10")) {
            ffn  = "C:\\Temp\\tradingapi_external_settings.txt";
        }
        else if (OS.equals("Linux")) {
            ffn = "/usr/local/tomcat/webapps/tradingapi_external_settings.txt";
        }
        else {
            System.out.println("Utility_ReadServerSettings> The OS " + OS + " is not supported!");      
            settings.put("status", "-1");
            settings.put("message", "The OS " + OS + " is not supported!");
        }  
        
        try {           
            File file = new File(ffn);  
            if (!file.exists()) {
                System.out.println("Utility_ReadServerSettings> The file " + ffn + " is not found!");
                settings.put("status", "-1");
                settings.put("message", "The file " + ffn + " is not found!");
                return settings;
            }
            if (!file.canRead()) {
                System.out.println("Utility_ReadServerSettings> No permissions to read the file " + ffn + " !");
                settings.put("status", "-1");
                settings.put("message", "No permissions to read the file " + ffn + " !");
                return settings;
            }
            if (file.length() == 0) {
                System.out.println("Utility_ReadServerSettings> The file " + ffn + " is empty!");
                settings.put("status", "-1");
                settings.put("message", "The file " + ffn + " is empty!");
                return settings;
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
             
            List<String[]> lines_array = new ArrayList<>();
            String data;        

            while ((data = br.readLine()) != null) {
                lines_array.add(data.split(":"));    
            }
            br.close();
                
            for (String[] array : lines_array) {
                settings.put(array[0], array[1].trim());
                System.out.println(Arrays.toString(array)); 
            }            
            settings.put("status", "OK");
        } catch (Exception e) { 
            e.printStackTrace();    
            settings.put("status", "-1");
            settings.put("message", "An exception occurred!");
        }
        
        return settings;
    } 
}
