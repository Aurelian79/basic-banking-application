package com.bank.servlet;

import com.bank.service.TransactionService;
import com.bank.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;


public class TransactionServlet extends BaseServlet {

    private TransactionService service = new TransactionService();

    static class DepositReq { int accountId; BigDecimal amount; }
    static class TransferReq { String fromAccountNumber; String toAccountNumber; BigDecimal amount; String description; }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        try {
            User currentUser = getSessionUser(req);

            String path = req.getPathInfo();
            if (path == null) {
                path = "";
            }

            if ("GET".equalsIgnoreCase(req.getMethod()) && (path.isEmpty() || "/".equals(path))) {
                int accountId = Integer.parseInt(req.getParameter("accountId"));
                LocalDate from = LocalDate.parse(req.getParameter("from"));
                LocalDate to = LocalDate.parse(req.getParameter("to"));
                sendJson(resp, 200, service.getTransactions(currentUser, accountId, from, to));

            } else if ("/deposit".equals(path)) {
                DepositReq dto = parseJson(readBody(req), DepositReq.class);
                service.deposit(currentUser, dto.accountId, dto.amount);
                sendJson(resp, 200, "Deposited");

            } else if ("/withdraw".equals(path)) {
                DepositReq dto = parseJson(readBody(req), DepositReq.class);
                service.withdraw(currentUser, dto.accountId, dto.amount);
                sendJson(resp, 200, "Withdrawn");

            } else if ("/transfer".equals(path)) {
                TransferReq dto = parseJson(readBody(req), TransferReq.class);
                service.transfer(currentUser, dto.fromAccountNumber, dto.toAccountNumber, dto.amount, dto.description);
                sendJson(resp, 200, "Transferred");

            } else {
                sendError(resp, 404, "Invalid route");
            }

        } catch (Exception e) {
            try { sendError(resp, 400, e.getMessage()); } catch (Exception ignored) {}
        }
    }
}
