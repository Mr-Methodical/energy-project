package com.example

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class EnergyServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/energydb";
    private static final String DB_USER = "energyuser";
    private static final String DB_PASS = "energy123";
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        // Allow anything to access the API
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Read query parameter:
        String country = request.getParameter("country");
        if (country == null || country.isEmpty()) {
            country = "Canada"; // default
        }

        PrintWriter out = response.getWriter();

        try {

        } catch (Exception e) {
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
