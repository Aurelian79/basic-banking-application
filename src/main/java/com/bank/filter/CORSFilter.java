package com.bank.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;

public class CORSFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PATCH,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        HttpServletRequest req = (HttpServletRequest) request;

        if ("OPTIONS".equals(req.getMethod())) {
            resp.setStatus(200);
            return;
        }

        chain.doFilter(request, response);
    }
}