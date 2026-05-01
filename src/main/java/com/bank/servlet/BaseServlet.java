package com.bank.servlet;


import com.bank.exception.BankException;
import com.bank.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;

public class BaseServlet extends HttpServlet {

    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                            src == null ? null : new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                            json == null || json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString()))
            .create();

    protected String readBody(HttpServletRequest request)throws IOException{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String line;

        while ((line = br.readLine()) != null)
            sb.append(line);
        return  sb.toString();
    }

    protected <T> T parseJson(String body,Class <T> clazz) throws BankException{
        if (body == null || body.trim().isEmpty()) {
            throw new BankException("Request body is required");
        }
        try {
            T parsed = gson.fromJson(body,clazz);
            if (parsed == null) {
                throw new BankException("Invalid JSON");
            }
            return parsed;
        }catch (Exception e){
            if (e instanceof BankException bankException) {
                throw bankException;
            }
            throw new BankException("Invalid JSON");
        }
    }

    protected void sendJson(HttpServletResponse response,int status,Object obj)throws IOException{
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(obj));
    }

    protected void sendError (HttpServletResponse response,int status,String msg)throws IOException{
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + msg + "\"}");
    }

    protected User getSessionUser(HttpServletRequest request) throws BankException{
        HttpSession session = request.getSession(false);
        if(session == null) throw new BankException("Not logged in");

        User user = (User) session.getAttribute("currentUser");
        if(user == null) throw new BankException("Not logged in");

        return user;
    }

}
