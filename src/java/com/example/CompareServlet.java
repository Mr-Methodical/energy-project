package com.example

import jakarta.servlet.*
import jakarta.servlet.http.*
import java.io.*
import java.sql.*

public class CompareServlet extends HttpServlet {

    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/energydb";
    private static final String DB_USER = "energyuser";
    private static final String DB_PASS = "energy123";

    // These are the only 10 countries shown in our frontend dropdown
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


    }
