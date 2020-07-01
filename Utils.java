package com.bezalel.trading_api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.bson.Document;
import org.codehaus.jettison.json.JSONObject; 
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class Utils {
    
    public String logTransferTXResults (
            double amount,
            String address_from,
            String address_to,
            String tx_result,
            int execution_time /* seconds */) throws Exception { 
        
        Settings settings = new Settings();
        String db_name = "logs";
        String collection = "transfers";
        
        JSONObject results_obj = new JSONObject();
        String date = new Date().toString();
        results_obj.put("date", date);        
        results_obj.put("pair", "ETH2TH"); 
        results_obj.put("amount", amount);
        results_obj.put("address_from", address_from);
        results_obj.put("address_to", address_to);
        results_obj.put("tx_result", tx_result);  
        results_obj.put("execution_time", execution_time);  
        
        // Insert TX results into MongoDB        
        try {
            MongoCredential credential = MongoCredential.createCredential(
                    settings.user, 
                    db_name, 
                    settings.password.toCharArray());
            MongoClient mongoClient = new MongoClient(new ServerAddress(
                    Settings.mongodb_host, 
                    settings.mongodb_port),
                    Arrays.asList(credential));
            
            Document doc = Document.parse(results_obj.toString());
            mongoClient.getDatabase(db_name).getCollection(collection).insertOne(doc);
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
       
        return results_obj.toString();
    }
    
    public String logTXResults (
            String pair, 
            double amount,
            boolean fixed_base_amount, 
            String address_base_from,
            String address_base_to,
            String address_quote_from,
            String address_quote_to,
            String base_exchangetx_results,
            String quote_exchangetx_results) throws Exception { 
        
        JSONObject results_obj = new JSONObject();
        String date = new Date().toString();
        results_obj.put("date", date);
        
        results_obj.put("pair", pair); 
        results_obj.put("amount", amount);
        results_obj.put("fixed_base_amount", fixed_base_amount);
        results_obj.put("address_base_from", address_base_from);
        results_obj.put("address_base_to", address_base_to);
        results_obj.put("address_quote_from", address_quote_from);
        results_obj.put("address_quote_to", address_quote_to);
            
        JSONObject txresults_obj = new JSONObject();
        txresults_obj.put("1st TX", quote_exchangetx_results);
        txresults_obj.put("2nd TX", base_exchangetx_results);

        results_obj.put("TX results", txresults_obj);  
        
        // Write results file
        Settings settings = new Settings();
        String txresults_file = null;
        String OS = System.getProperty("os.name");
        if (OS.equals("Windows 10")) {
            // Add TX results to log file
            txresults_file = settings.win10_store + "\\txresults_file.txt";            
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(new File(txresults_file), true));
                writer.write(results_obj.toString() + "\n");
            } catch (Exception e) {        
                e.printStackTrace();
            }           
            writer.close();    
        }
        else if (OS.equals("Linux")) {
            // Insert TX results into MongoDB             
            try {
                String db_name = "logs";
                MongoCredential credential = MongoCredential.createCredential(
                        settings.user, 
                        db_name, 
                        settings.password.toCharArray());
                MongoClient mongoClient = new MongoClient(new ServerAddress(
                        settings.mongodb_host, 
                        settings.mongodb_port),
                        Arrays.asList(credential));
                
                Document doc = Document.parse(results_obj.toString());
                mongoClient.getDatabase(db_name).getCollection("sandbox_transactions").insertOne(doc);
                mongoClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }        
       
        return results_obj.toString();
    }
   
    public class TrippleDes {

        private static final String UNICODE_FORMAT = "UTF8";
        public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
        private KeySpec ks;
        private SecretKeyFactory skf;
        private Cipher cipher;
        byte[] arrayBytes;
        private String myEncryptionKey;
        private String myEncryptionScheme;
        SecretKey key;

        public TrippleDes() throws Exception {
            Settings settings = new Settings();
            myEncryptionKey = settings.EncryptionKey;            
            myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
            arrayBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
            ks = new DESedeKeySpec(arrayBytes);
            skf = SecretKeyFactory.getInstance(myEncryptionScheme);
            cipher = Cipher.getInstance(myEncryptionScheme);
            key = skf.generateSecret(ks);
        }

        public String encrypt(String unencryptedString) {
            String encryptedString = null;
            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
                byte[] encryptedText = cipher.doFinal(plainText);
                encryptedString = new String(Base64.encodeBase64(encryptedText));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return encryptedString;
        }

        public String decrypt(String encryptedString) {
            String decryptedText=null;
            try {
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] encryptedText = Base64.decodeBase64(encryptedString);
                byte[] plainText = cipher.doFinal(encryptedText);
                decryptedText= new String(plainText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return decryptedText;
        }
    } 
    
    public String makeJSONFromWallet (
            String address, 
            String password_encrypted,
            String network,
            String user, 
            String walletname, 
            String wallet_data) throws Exception { 
        
        JSONObject results_obj = new JSONObject();
        
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = dateFormat.format(new Date());
        results_obj.put("symbol", "ETH");
        results_obj.put("address", address); 
        results_obj.put("password", password_encrypted); 
        results_obj.put("network", network); 
        results_obj.put("user", user); 
        results_obj.put("date_created",  date); 

        JSONObject wallet_obj = new JSONObject();
        wallet_obj.put("name", walletname);
        
        JSONObject dataobj = new JSONObject(wallet_data);
        wallet_obj.put("data", dataobj);            
        results_obj.put("wallet", wallet_obj);       
         
        results_obj.put("notes", "Valid for other testnets and mainnet also");
        
        return results_obj.toString();
    }
    
    public String makeJSON_WavesWallet (
            String address, 
            String seed_encrypted, 
            String network, 
            String user, 
            String walletdata_encrypted,
            String notes) throws Exception { 
        
        JSONObject results_obj = new JSONObject();
        
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = dateFormat.format(new Date());
        
        results_obj.put("symbol", "WAVES");
        results_obj.put("address", address); 
        results_obj.put("seed", seed_encrypted); 
        results_obj.put("network", network); 
        results_obj.put("user", user);
        results_obj.put("date_created",  date);
        results_obj.put("wallet", walletdata_encrypted);
        results_obj.put("notes", notes);
        
        return results_obj.toString();
    }
    
    // Limitation: the function does not work if line/file is not terminated
    public String readFile(String wallet_ffn) throws Exception {      
        File file = new File(wallet_ffn);         
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        
        // Read one line
        String data = br.readLine(); 
        br.close();

        return data;
    } 
    
    // Reads files including files with no terminating characters
    public String readFileAsString(String ffn) throws Exception {
       File file = new File(ffn); 
       if (!file.exists()) return "The file " + ffn + " is not found!";
       if (!file.canRead()) return "No permissions to read the file " + ffn + " !";
       if (file.length() == 0) return "The file " + ffn + " is empty!";

       String data = new String(Files.readAllBytes(Paths.get(ffn)));
       return data.toString();
    } 
    
    public int utilityGetRandomNumber(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        
        return randomNum;
    }
    
    public byte[] decodeBase58To25Bytes(String input) {
        final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        BigInteger num = BigInteger.ZERO;
        for (char t : input.toCharArray()) {
            int p = ALPHABET.indexOf(t);
            if (p == -1)
                return null;
            num = num.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(p));
        }
 
        byte[] result = new byte[25];
        byte[] numBytes = num.toByteArray();
        System.arraycopy(numBytes, 0, result, result.length - numBytes.length, numBytes.length);
        return result;
    }
 
    public byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }    
}
