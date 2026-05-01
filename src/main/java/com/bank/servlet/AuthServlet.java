package com.bank.servlet;

import com.bank.service.AuthService;
import com.bank.model.User;
import com.bank.exception.BankException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;


public class AuthServlet extends BaseServlet {

    private AuthService service = new AuthService();

    static class LoginReq {
        String email, password;
    }
    static class RegisterReq {
        String fullName, email, phone, password;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        try {
            String path = req.getPathInfo();
            if (path == null) {
                path = "";
            }

            if ("/register".equals(path)) {
                if (!"POST".equalsIgnoreCase(req.getMethod())) {
                    sendError(resp, 405, "Method not allowed");
                    return;
                }
                RegisterReq dto = parseJson(readBody(req), RegisterReq.class);
                if (dto.fullName == null || dto.email == null || dto.phone == null || dto.password == null) {
                    sendError(resp, 400, "All registration fields are required");
                    return;
                }
                User u = service.register(dto.fullName, dto.email, dto.phone, dto.password);
                sendJson(resp, 201, u);

            } else if ("/login".equals(path)) {
                if (!"POST".equalsIgnoreCase(req.getMethod())) {
                    sendError(resp, 405, "Method not allowed");
                    return;
                }
                LoginReq dto = parseJson(readBody(req), LoginReq.class);
                if (dto.email == null || dto.password == null) {
                    sendError(resp, 400, "Email and password are required");
                    return;
                }
                User u = service.login(dto.email, dto.password);

                req.getSession(true).setAttribute("currentUser", u);
                sendJson(resp, 200, u);

            } else if ("/logout".equals(path)) {
                if (!"POST".equalsIgnoreCase(req.getMethod())) {
                    sendError(resp, 405, "Method not allowed");
                    return;
                }
                User u = getSessionUser(req);
                service.logout(u.getUserId());

                req.getSession(false).invalidate();
                sendJson(resp, 200, "Logged out");

            } else {
                sendError(resp, 404, "Invalid route");
            }

        } catch (BankException e) {
            try { sendError(resp, 400, e.getMessage()); } catch (Exception ignored) {}
        } catch (Exception e) {
            try { sendError(resp, 500, "Internal server error"); } catch (Exception ignored) {}
        }
    }
}
