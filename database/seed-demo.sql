INSERT INTO users (full_name, email, phone, password_hash, role, is_active)
VALUES (
    'Demo User',
    'demo@vaultx.local',
    '9999999999',
    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f',
    'CUSTOMER',
    TRUE
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO accounts (account_number, user_id, account_type, balance, status, interest_rate)
SELECT
    'BNK1000000001',
    u.user_id,
    'SAVINGS',
    25000.00,
    'ACTIVE',
    3.50
FROM users u
WHERE u.email = 'demo@vaultx.local'
  AND NOT EXISTS (
      SELECT 1
      FROM accounts a
      WHERE a.account_number = 'BNK1000000001'
  );

INSERT INTO transactions (to_account_id, txn_type, amount, balance_after, description, reference_no)
SELECT
    a.account_id,
    'DEPOSIT',
    25000.00,
    25000.00,
    'Initial demo funding',
    'DEMO-TXN-0001'
FROM accounts a
WHERE a.account_number = 'BNK1000000001'
  AND NOT EXISTS (
      SELECT 1
      FROM transactions t
      WHERE t.reference_no = 'DEMO-TXN-0001'
  );
