package com.bezalel.trading_api;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;

import java.io.PrintWriter; 

@WebServlet("/PayPal_Servlet")
public class PayPal_Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
       
    public PayPal_Servlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Settings settings = new Settings();

        PrintWriter out = response.getWriter();     
        String paymentId = request.getParameter("paymentId");
        out.println(paymentId);
        String token = request.getParameter("token");
        out.println(token);
        String PayerID = request.getParameter("PayerID");
        out.println(PayerID);
        out.close();
        
        if (paymentId != null && PayerID != null) {
            System.out.println("The arguments are valid!");
            
            APIContext apiContext = null;			
            try {
                apiContext = new APIContext(settings.clientId, settings.clientSecret, "sandbox");
                System.out.println("doGet> Servlet access token: " + apiContext.fetchAccessToken());
            }
            catch (Exception ex) {
                System.out.println("doGet> APIContext error");	
            }
            
            Payment payment = new Payment();
            payment.setId(paymentId);
    
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(PayerID);
            
            try {
                Payment createdPayment = payment.execute(apiContext, paymentExecution);
                System.out.println("doGet> payment.execute");	
                System.out.println(createdPayment);
            } catch (Exception ex) {
                System.out.println("doGet> ExecutePayment error");	
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }   
}




