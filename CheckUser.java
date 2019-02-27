package com.bezalel.trading_api;

import java.sql.*;
import java.lang.System;

public class CheckUser 
{
    public String ValidateUser(String _user, String _pwd)
    {
        String url = "jdbc:mysql://db4free.net:3306/tradingapi_login";
        int count = 0;

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, "user_p**e", "user_p**e");
            String sql = "SELECT * FROM tradingapi_login.Accounts WHERE email = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, _user);
            ps.setString(2, _pwd);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
                count++;

            rs.close();
            conn.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (count == 0)
            return "The user does not exist";
        else
            return "The user is validated!";
    }

    public String RegisterUser(String _user, String _pwd)
    {
        String url = "jdbc:mysql://db4free.net:3306/tradingapi_login";
        int count = 0;

        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, "user_p**e", "user_p**e");
            String sql = "SELECT COUNT(*) FROM tradingapi_login.Accounts WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, _user);
            ResultSet rs = ps.executeQuery();

            rs.next();
            count = rs.getInt(1);

            if (count != 0)
            {
                rs.close();
                conn.close();
                return "The user already registered!";
            }
            else
            {
                sql = "INSERT INTO tradingapi_login.Accounts (email, password) VALUES (?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, _user);
                ps.setString(2, _pwd);
                ps.execute();
            }
            rs.close();    
            conn.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "The user " + _user + " is registered!";
    }
}
