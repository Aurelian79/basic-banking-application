package com.bank.servlet;

import com.bank.service.LoanService;
import com.bank.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.math.BigDecimal;


public class LoanServlet extends BaseServlet {

    private LoanService service = new LoanService();

    static class Req { int accountId; BigDecimal amount; int months; }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        try {
            User user = getSessionUser(req);
            String path = req.getPathInfo();
            if (path == null) {
                path = "";
            }

            if ("GET".equalsIgnoreCase(req.getMethod()) && (path.isEmpty() || "/".equals(path))) {
                sendJson(resp, 200, service.getLoans(user.getUserId()));

            } else if ("/apply".equals(path)) {
                Req dto = parseJson(readBody(req), Req.class);
                sendJson(resp, 201,
                        service.apply(user, dto.accountId, dto.amount, dto.months));

            } else {
                sendError(resp, 404, "Invalid route");
            }

        } catch (Exception e) {
            try { sendError(resp, 400, e.getMessage()); } catch (Exception ignored) {}
        }
    }
}
