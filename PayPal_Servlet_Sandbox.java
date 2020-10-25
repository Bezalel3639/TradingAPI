package com.bezalel.trading_api;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import java.io.PrintWriter;
import java.util.Arrays; 

@WebServlet("/PayPal_Servlet_Sandbox")
public class PayPal_Servlet_Sandbox extends HttpServlet {

    private static final long serialVersionUID = 1L;
       
    public PayPal_Servlet_Sandbox() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Settings settings = new Settings();
        Utils utils = new Utils();

        PrintWriter out = response.getWriter();		
        
        String paymentId = request.getParameter("paymentId");
        out.println("DoGet> paymentId: " + paymentId);
        String token = request.getParameter("token");
        out.println("DoGet> token: " + token);
        String PayerID = request.getParameter("PayerID");
        out.println("DoGet> PayerID: " + PayerID);
        out.close();
        
        if (paymentId != null && PayerID != null) {
            System.out.println("DoGet> The arguments are valid!");
            
            APIContext apiContext = null;			
            try {
                apiContext = new APIContext(settings.clientId, settings.clientSecret, "sandbox");
                System.out.println("DoGet> Servlet access token: " + apiContext.fetchAccessToken());
            }
            catch (Exception ex) {
                System.out.println("DoGet> APIContext error");	
            }
            
            Payment payment = new Payment();
            payment.setId(paymentId);
    
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(PayerID);    	
            
            try {
                Payment createdPayment = payment.execute(apiContext, paymentExecution);
                System.out.println("DoGet> payment.execute");	
                System.out.println("DoGet> createdPayment: " + createdPayment);
                
                JSONObject obj = new JSONObject(createdPayment.toJSON());
                JSONObject obj_payer = obj.getJSONObject("payer");
                System.out.println("DoGet> payment_method: " + obj_payer.getString("payment_method")); 
                
                JSONObject obj_payer_info = obj_payer.getJSONObject("payer_info");
                String emailfrom = obj_payer_info.getString("email");
                System.out.println("DoGet> email: " + emailfrom);   
                
                JSONArray array_obj = obj.getJSONArray("transactions");
                System.out.println(array_obj.getJSONObject(0).toString());
                JSONObject obj_amount = array_obj.getJSONObject(0).getJSONObject("amount");
                String currency = obj_amount.getString("currency");
                String amount = obj_amount.getString("total");
                System.out.println("DoGet> Currency: " + currency); 
                System.out.println("DoGet> Amount: " + amount); 
                
                JSONArray array_related_resources = array_obj.getJSONObject(0).getJSONArray("related_resources");
                JSONObject obj_sale = array_related_resources.getJSONObject(0).getJSONObject("sale");
                JSONObject transaction_fee = obj_sale.getJSONObject("transaction_fee");
                String fee = transaction_fee.getString("value");
                System.out.println("DoGet> Fee: " + fee);   	    
                
                String notes = "";
                utils.logFiatDepositResults(
                    currency, 
                    Double.valueOf(amount), 
                    Double.valueOf(fee), 
                    "PayPal", 
                    emailfrom, 
                    settings.test_user, 
                    PayerID, 
                    paymentId, 
                    token, 
                    createdPayment.toString(), 
                    notes);
                
                updateFiatBalance(
                        emailfrom, 
                        currency, 
                        Double.valueOf(amount) - Double.valueOf(fee));
            } catch (Exception ex) {
                System.out.println("doGet> ExecutePayment error");	
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    } 
    
    protected void updateFiatBalance(
            String email, 
            String currency, 
            double balance_update_amount) throws Exception { 
            
        Settings settings = new Settings();
        String user = "db_operator";
        String db_name = "wallets";
        String db_collection = "";
        double current_balance = 0.0;
         
        switch (currency)
        {
            case ("RUB"):
                db_collection = "RUB (sandbox)";
                break;                
            case ("EUR"):
                db_collection = "EUR (sandbox)";
                break; 
            case ("HKD"):
                db_collection = "HKD (sandbox)";
                break; 
            case ("ILS"):
                db_collection = "ILS (sandbox)";
                break; 
            case ("USD"):
                db_collection = "USD (sandbox)";
                break;
        }        
        
        MongoCredential credential = MongoCredential.createCredential(
            user, db_name, 
            settings.password.toCharArray());
        MongoClient mongoClient = new MongoClient(new ServerAddress(
            Settings.mongodb_host, 
            settings.mongodb_port),
            Arrays.asList(credential));
             
        MongoDatabase db = mongoClient.getDatabase(db_name);
        MongoCollection<Document> collection = db.getCollection(db_collection);
        
        Document document = collection.find(eq("email", email)).first();
         
        if (document == null) { 
            System.out.println("Servlet> UpdateFiatBalance> Nothing was found in DB");
        } else { 
            System.out.println("Servlet> UpdateFiatBalance> A record was found in the DB!"); 
            JSONObject obj = new JSONObject(document.toJson());
            System.out.println(obj.toString()); 
            current_balance = obj.getDouble("balance");
            System.out.println("Servlet> UpdateFiatBalance> Current balance: " + current_balance);  
            
            // Update balance
            BasicDBObject query = new BasicDBObject(); 
            query.put("email", email);
            BasicDBObject data = new BasicDBObject(); 
            data.put("balance", current_balance + balance_update_amount); 
            System.out.println("Servlet> UpdateFiatBalance> New balance: " + current_balance + balance_update_amount);  
            BasicDBObject command = new BasicDBObject();
            command.put("$set", data);
            collection.updateOne(query, command);            
        }; 
        
        mongoClient.close();
    }
}