package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.util.FileLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportService — Phase 6 File I/O + Collections Showcase
 *
 * Generates account statements as .csv files on disk.
 * generateSummary() uses HashMap and ArrayList (Collections showcase).
 * generateStatement() uses BufferedWriter + FileWriter (File I/O showcase).
 *
 * Output directory: logs/reports/
 * File naming: statement_ACC001_2024-01-01_2024-03-31.csv
 */
public class ReportService {

    private TransactionDAO txnDAO       = new TransactionDAO();
    private AccountDAO     accountDAO   = new AccountDAO();
    private FileLogger fileLogger = FileLogger.getInstance();

    private static final String REPORT_DIR = "logs/reports/";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter GENERATED_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    // ── GENERATE CSV STATEMENT ──────────────────────────────────────────────
    /**
     * Fetches all transactions for an account in the date range,
     * writes them as a CSV file to disk, returns the file path.
     *
     * The servlet reads the file and streams it as a download response.
     *
     * File I/O flow:
     *   1. Create logs/reports/ directory if it doesn't exist
     *   2. Open FileWriter in overwrite mode (new report each request)
     *   3. Wrap in BufferedWriter for efficient writes
     *   4. Write header + one row per transaction
     *   5. flush() → close() in finally block
     *
     * @return absolute file path of the generated CSV
     */
    public String generateStatement(User currentUser, int accountId, LocalDate from, LocalDate to)
            throws BankException {

        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new BankException("Account not found");
        }
        validateOwnership(currentUser, account);

        // ── 1. Ensure output directory exists ──────────────────────────────
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) dir.mkdirs();      // mkdirs = create full path including parents

        // ── 2. Build file path ─────────────────────────────────────────────
        String fileName = String.format("statement_%d_%s_%s.csv",
                accountId, from.format(DATE_FMT), to.format(DATE_FMT));
        String filePath = REPORT_DIR + fileName;

        // ── 3. Fetch data ──────────────────────────────────────────────────
        // TransactionDAO.findByAccountId returns List<Transaction> (ArrayList internally)
        List<Transaction> txns;
        try {
            txns = txnDAO.findByAccountId(accountId, from, to, null);
            // null conn = DAO opens its own connection (read-only, no tx needed)
        } catch (Exception e) {
            throw new BankException("Failed to fetch transactions for report", e);
        }

        // ── 4. Write CSV ───────────────────────────────────────────────────
        // FileWriter(path, false) = overwrite mode (fresh report each time)
        // BufferedWriter batches the writes — no disk hit on every bw.write() call
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filePath, false));

            // Header row
            bw.write("TxnID,Type,Amount,BalanceAfter,Description,ReferenceNo,Timestamp");
            bw.newLine();

            // One row per transaction
            for (Transaction t : txns) {           // iterating ArrayList from DAO
                bw.write(buildCsvRow(t));
                bw.newLine();
            }

            // ── 5. Flush + summary footer ──────────────────────────────────
            bw.write("");
            bw.newLine();
            bw.write("# Generated: " + generatedAt());
            bw.newLine();
            bw.write("# Account ID: " + accountId);
            bw.newLine();
            bw.write("# Period: " + from + " to " + to);
            bw.newLine();
            bw.write("# Total Transactions: " + txns.size());
            bw.newLine();

            bw.flush();    // force all buffered bytes to disk

        } catch (IOException e) {
            throw new BankException("Failed to write report file", e);
        } finally {
            // Always close — releases OS file handle even if write failed
            if (bw != null) {
                try { bw.close(); } catch (IOException ignored) {}
            }
        }

        return filePath;
    }

    // ── GENERATE SUMMARY (Collections showcase) ─────────────────────────────
    /**
     * Aggregates stats across all user accounts using HashMap + ArrayList.
     * Returns a Map ready to be serialized to JSON by the servlet.
     *
     * Collections used:
     *   - List<Account>      from accountDAO (ArrayList internally)
     *   - List<Transaction>  from txnDAO (ArrayList internally)
     *   - HashMap<String, Object> to hold per-account summary
     *   - List<Map> to hold the array of account summaries
     */
    public Map<String, Object> generateSummary(int userId) throws BankException {

        // ── Outer result map ───────────────────────────────────────────────
        Map<String, Object> result = new HashMap<>();

        // ── Fetch all accounts for user ────────────────────────────────────
        List<Account> accounts;
        try {
            accounts = accountDAO.findUserById(userId);
        } catch (Exception e) {
            throw new BankException("Failed to load accounts", e);
        }

        // ── Per-account summaries — stored in an ArrayList of Maps ─────────
        List<Map<String, Object>> accountSummaries = new ArrayList<>();

        BigDecimal totalBalance     = BigDecimal.ZERO;
        BigDecimal totalDeposited   = BigDecimal.ZERO;
        BigDecimal totalWithdrawn   = BigDecimal.ZERO;

        for (Account acc : accounts) {            // iterating ArrayList<Account>

            // Fetch last 30 days of transactions for this account
            List<Transaction> txns;
            try {
                txns = txnDAO.findByAccountId(
                        acc.getAccountId(),
                        LocalDate.now().minusDays(30),
                        LocalDate.now(),
                        null
                );
            } catch (Exception e) {
                txns = new ArrayList<>();          // don't fail whole report if one account has no txns
            }

            // Aggregate using loop over ArrayList
            BigDecimal deposited = BigDecimal.ZERO;
            BigDecimal withdrawn = BigDecimal.ZERO;

            for (Transaction t : txns) {           // iterating ArrayList<Transaction>
                if ("DEPOSIT".equals(t.getTxnType()) || "LOAN_DISBURSEMENT".equals(t.getTxnType())) {
                    deposited = deposited.add(t.getAmount());
                } else if ("WITHDRAWAL".equals(t.getTxnType()) || "TRANSFER".equals(t.getTxnType())) {
                    withdrawn = withdrawn.add(t.getAmount());
                }
            }

            // Build per-account HashMap
            Map<String, Object> summary = new HashMap<>();
            summary.put("accountId",     acc.getAccountId());
            summary.put("accountNumber", acc.getAccountNumber());
            summary.put("accountType",   acc.getAccountType());
            summary.put("balance",       acc.getBalance());
            summary.put("status",        acc.getStatus());
            summary.put("deposited30d",  deposited);
            summary.put("withdrawn30d",  withdrawn);
            summary.put("netFlow30d",    deposited.subtract(withdrawn));
            summary.put("txnCount30d",   txns.size());

            accountSummaries.add(summary);         // add HashMap to ArrayList

            // Running totals
            totalBalance   = totalBalance.add(acc.getBalance() != null ? acc.getBalance() : BigDecimal.ZERO);
            totalDeposited = totalDeposited.add(deposited);
            totalWithdrawn = totalWithdrawn.add(withdrawn);
        }

        // ── Build top-level result HashMap ─────────────────────────────────
        result.put("userId",         userId);
        result.put("totalAccounts",  accounts.size());
        result.put("totalBalance",   totalBalance);
        result.put("totalDeposited", totalDeposited);
        result.put("totalWithdrawn", totalWithdrawn);
        result.put("netFlow",        totalDeposited.subtract(totalWithdrawn));
        result.put("accounts",       accountSummaries);   // List<Map> nested in Map
        result.put("generatedAt",    generatedAt());

        return result;
    }

    // ── CSV ROW BUILDER ─────────────────────────────────────────────────────
    private String buildCsvRow(Transaction t) {
        return String.join(",",
                str(t.getTxnId()),
                escape(t.getTxnType()),
                str(t.getAmount()),
                str(t.getBalanceAfter()),
                escape(t.getDescription()),
                escape(t.getReferenceNo()),
                t.getTxnTimestamp() != null ? t.getTxnTimestamp().format(TS_FMT) : ""
        );
    }

    private String str(Object o)    { return o == null ? "" : o.toString(); }
    private String generatedAt() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        return now.format(GENERATED_FMT);
    }

    private String escape(String s) {
        if (s == null) return "";
        // Wrap in quotes if value contains comma or newline
        if (s.contains(",") || s.contains("\n")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private void validateOwnership(User currentUser, Account account) {
        if (account.getUserId() != currentUser.getUserId()) {
            fileLogger.log(
                    currentUser.getUserId(),
                    "UNAUTHORIZED_ACCESS",
                    "ACCOUNT",
                    account.getAccountId(),
                    "N/A",
                    "User tried to download statement for another user's account"
            );
            throw new SecurityException("Unauthorized access");
        }
    }
}
