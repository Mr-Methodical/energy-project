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
            // using ? to prevent SQL injection attacks:
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
            stmt.setString(1, country);
            ResultSet rs = stmt.executeQuery();

            // Now we are creating the JSON manually:
            out.println("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.println(",");
                out.print("  {");
                out.print("\"year\": " + rs.getInt("year") + ", ");
                out.print("\"renewables\" +
                          rs.getDouble("renewables_electricity") + ", ");
                out.print("\"fossil\" +
                          rs.getDouble("fossil_electricity") + ", ");
                out.print("\"nuclear\" +
                          rs.getDouble("nuclear_electricity"));
                out.print("}");
                first = false;
            }
            out.println("\n]");
            // release memory back:
            rs.close();   // we no longer need rows we were looping through
            stmt.close(); // done with prepared statement (template of query)
            conn.close(); // done with database close connection 
        } catch (Exception e) {
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
