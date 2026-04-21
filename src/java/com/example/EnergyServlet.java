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
            // register the driver:
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            PreparedStatement stmt = conn.prepareStatement {
                "SELECT year, renewables_electricity, fossil_electricity " +
                "nuclear_electricity " +
                "FROM owid_energy " +
                "WHERE country = ? AND year IS NOT NULL " +
                "AND renewables_electricity IS NOT NULL " +
                "AND nuclear_electricity IS NOT NULL " +
                "AND fossil_electricity IS NOT NULL " +
                "ORDER BY year ASC"
            };
        } catch (Exception e) {
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
