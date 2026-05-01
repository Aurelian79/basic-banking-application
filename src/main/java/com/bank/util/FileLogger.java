package com.bank.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FileLogger — Phase 6 File I/O Showcase
 *
 * Appends structured audit entries to a rolling daily log file.
 * Every sensitive action (login, transfer, loan approval) calls this
 * IN ADDITION to the DB AuditLogDAO, so there is a file-based trail
 * that survives even if the DB is wiped.
 *
 * Log location: logs/audit/audit_YYYY-MM-DD.log
 * Format per line:
 *   [2024-06-01 14:32:05] | USER:3 | ACTION:TRANSFER | ENTITY:TRANSACTION |
 *   ENTITY_ID:42 | IP:192.168.1.1 | DETAIL:Transferred 5000 to BNK001
 *
 * File I/O concepts used:
 *   - File / mkdir for directory creation
 *   - FileWriter with append=true so old entries are never overwritten
 *   - BufferedWriter for efficiency (batches writes, single syscall)
 *   - flush() to force write to disk before close
 *   - try-with-resources ensures close() is always called
 */
public class FileLogger {

    // Singleton — one logger for the entire app
    private static FileLogger instance;

    // Root directory for all log files
    private static final String LOG_DIR = "logs/audit/";

    // Date format used in file names for daily rotation
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Timestamp format written inside each log line
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileLogger() {
        // Ensure the log directory exists on first use
        // mkdirs() creates the full path including parent directories
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static synchronized FileLogger getInstance() {
        if (instance == null) instance = new FileLogger();
        return instance;
    }

    /**
     * Appends one audit line to today's log file.
     *
     * @param userId   ID of the user performing the action (0 if system)
     * @param action   e.g. "LOGIN", "TRANSFER", "LOAN_APPROVE"
     * @param entity   e.g. "TRANSACTION", "ACCOUNT", "LOAN" (nullable)
     * @param entityId DB primary key of the entity touched (0 if N/A)
     * @param ip       Client IP address from HttpServletRequest
     * @param detail   Free-text description of what happened
     */
    public synchronized void log(int userId, String action, String entity,
                                 int entityId, String ip, String detail) {

        // Build today's log file path  e.g. logs/audit/audit_2024-06-01.log
        String today    = LocalDate.now().format(DATE_FMT);
        String filePath = LOG_DIR + "audit_" + today + ".log";

        // Build the log line
        String timestamp = LocalDateTime.now().format(TS_FMT);
        String line = String.format(
                "[%s] | USER:%d | ACTION:%-20s | ENTITY:%-15s | ENTITY_ID:%-6d | IP:%-15s | %s",
                timestamp, userId, action,
                entity  == null ? "—" : entity,
                entityId,
                ip      == null ? "unknown" : ip,
                detail  == null ? "" : detail
        );

        // FileWriter(path, true) = append mode — never overwrites existing content
        // BufferedWriter wraps it for efficiency — writes are buffered in memory
        // and flushed as a single OS write call
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(line);
            bw.newLine();           // platform-safe line separator
            bw.flush();             // ensure it hits disk before close
        } catch (IOException e) {
            // Log file write failure must never crash the main request
            // Print to stderr so Tomcat catalina.out catches it
            System.err.println("[FileLogger] Failed to write audit log: " + e.getMessage());
        }
    }

    /**
     * Convenience overload — no entity reference needed
     */
    public void log(int userId, String action, String ip, String detail) {
        log(userId, action, null, 0, ip, detail);
    }
}
