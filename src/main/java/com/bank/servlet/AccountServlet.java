package com.bank.servlet;

import com.bank.service.AccountService;
import com.bank.model.Account;
import com.bank.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.util.List;


public class AccountServlet extends BaseServlet {

    private AccountService service = new AccountService();

    static class Req { String accountType; }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        try {
            User user = getSessionUser(req);

            if ("GET".equals(req.getMethod())) {
                List<Account> list = service.getAccounts(user.getUserId());
                sendJson(resp, 200, list);

            } else if ("POST".equals(req.getMethod())) {
                Req dto = parseJson(readBody(req), Req.class);
                Account acc = service.openAccount(user.getUserId(), dto.accountType);
                sendJson(resp, 201, acc);

            } else {
                sendError(resp, 404, "Invalid");
            }

        } catch (Exception e) {
            try { sendError(resp, 400, e.getMessage()); } catch (Exception ignored) {}
        }
    }
}