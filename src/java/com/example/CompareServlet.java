package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;

public class CompareServlet extends HttpServlet {

    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/energydb";
    private static final String DB_USER = "energyuser";
    private static final String DB_PASS = "energy123";

    // These are the only 10 countries shown in our dropdown
    private static final String[] COUNTRIES = {
        "United States", "Canada", "China", "Germany", "France",
        "India", "United Kingdom", "Brazil", "Japan", "Australia"
    };
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String type = request.getParameter("type");
        String year = request.getParameter("year");

        if (type == null || type.isEmpty()) type = "nuclear";
        if (year == null || year.isEmpty()) year = "2023";
        
        String column;
        switch(type.toLowerCase()) {
            case "renewables": column = "renewables_electricity"; break;
            case "fossil": column = "fossil_electricity"; break;
            case "nuclear": column = "nuclear_electricity"; break;
            default:
                response.getWriter().println("{\"error\": \"Invalid Type\"}");
                return;
        }
        PrintWriter out = response.getWriter();
        try {
            // telling DriverManager that we want to use postgres driver
            Class.forName("org.postgresql.Driver");
            // Connects to posgres:
            Connection conn =
                DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            // sending SQL template for postgres to optimize:
            // Note: putting column in which is safe as it can only be 
            //   1 of 3 things we specified
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT country, " + column + " AS value " +
                "FROM owid_energy " +
                "WHERE country = ? AND year = ? " + 
                "AND " + column + " IS NOT NULL " + 
                "AND " + column + " != ''"
            );
            out.println("[");
            boolean first = true;

            for (String country : COUNTRIES) {
                stmt.setString(1, country);
                stmt.setString(2, year);
                // only one specific row for a country and a year
                //   so rs should only be one row:
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    if (!first) out.println(",");
                    out.print("  {");
                    out.print("\"country\": \"" + country + "\", ");
                    out.print("\"value\": " + rs.getDouble("value"));
                    out.print("}");
                    first = false;
                }
                rs.close();
            }
            out.println("\n]");
            stmt.close();
            conn.close();
        } catch (Exception e) {
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
