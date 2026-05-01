package com.bank.servlet;

import com.bank.model.User;
import com.bank.service.ReportService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * ReportServlet — upgraded for Phase 6
 *
 * GET /api/reports/statement?accountId=X&from=YYYY-MM-DD&to=YYYY-MM-DD
 *   → generates CSV file via ReportService → streams it as download
 *
 * GET /api/reports/summary
 *   → returns aggregated stats as JSON (HashMap → Gson → JSON)
 */

public class ReportServlet extends BaseServlet {

    private ReportService service = new ReportService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            User user = getSessionUser(req);
            String path = req.getPathInfo();

            if ("/statement".equals(path) && "GET".equals(req.getMethod())) {
                handleStatement(req, resp, user);

            } else if ("/summary".equals(path) && "GET".equals(req.getMethod())) {
                handleSummary(req, resp, user);

            } else {
                sendError(resp, 404, "Route not found");
            }

        } catch (Exception e) {
            try { sendError(resp, 400, e.getMessage()); } catch (Exception ignored) {}
        }
    }

    // ── STATEMENT DOWNLOAD ──────────────────────────────────────────────────
    private void handleStatement(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {

        // Parse query params
        String accIdStr  = req.getParameter("accountId");
        String fromStr   = req.getParameter("from");
        String toStr     = req.getParameter("to");

        if (accIdStr == null || fromStr == null || toStr == null)
            throw new Exception("accountId, from, and to query params required");

        int       accountId = Integer.parseInt(accIdStr);
        LocalDate from      = LocalDate.parse(fromStr);
        LocalDate to        = LocalDate.parse(toStr);

        if (from.isAfter(to))
            throw new Exception("'from' date cannot be after 'to' date");

        // Generate the CSV file — ReportService writes it to disk
        String filePath = service.generateStatement(accountId, from, to);

        // Stream the file bytes back as a download
        File file = new File(filePath);
        String fileName = file.getName();

        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentLengthLong(file.length());

        // FileInputStream reads the file bytes chunk by chunk
        // Write them directly into the HTTP response output stream
        try (FileInputStream fis  = new FileInputStream(file);
             OutputStream    out  = resp.getOutputStream()) {

            byte[] buffer = new byte[4096];   // 4KB read buffer
            int    bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
        }
    }

    // ── SUMMARY JSON ────────────────────────────────────────────────────────
    private void handleSummary(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {

        // ReportService returns a HashMap<String, Object>
        // Gson converts it to JSON automatically — nested maps, lists all handled
        Map<String, Object> summary = service.generateSummary(user.getUserId());
        sendJson(resp, 200, summary);
    }
}
