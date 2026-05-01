(function () {
  const API_BASE = window.API_BASE || '/banking-system/api';
  const TITLES = {
    overview: 'Overview',
    accounts: 'Accounts',
    transfer: 'Transfer',
    loans: 'Loans',
    reports: 'Reports'
  };

  document.addEventListener('DOMContentLoaded', init);

  window.showSection = showSection;
  window.handleOpenAccount = handleOpenAccount;
  window.handleDeposit = handleDeposit;
  window.handleWithdraw = handleWithdraw;
  window.handleTransfer = handleTransfer;
  window.handleLoanApply = handleLoanApply;
  window.handleReport = handleReport;
  window.showModal = showModal;
  window.closeModal = closeModal;
  window.closeModalById = closeModalById;

  function init() {
    const user = requireAuth();
    text('topbarUser', user.fullName || user.email);
    text('welcomeText', `Welcome, ${firstName(user)}`);
    setDefaultReportDates();
    loadOverview();
  }

  function showSection(name) {
    document.querySelectorAll('.section').forEach((section) => {
      section.classList.toggle('active', section.id === name);
    });

    document.querySelectorAll('.nav-item').forEach((button) => {
      button.classList.toggle('active', button.dataset.section === name);
    });

    text('sectionTitle', TITLES[name] || 'Dashboard');

    if (name === 'accounts') loadAccounts();
    if (name === 'loans') loadLoans();
  }

  async function loadOverview() {
    try {
      const accounts = await api('/accounts');
      html('accountCards', accounts.length
        ? accounts.map(renderAccountCard).join('')
        : empty('No accounts found.'));

      if (!accounts.length) {
        html('recentTxns', empty('No transactions found.'));
        return;
      }

      const first = accounts[0].accountId;
      const txns = await api(`/transactions?accountId=${first}&from=2000-01-01&to=2099-12-31`);
      html('recentTxns', txns.length
        ? txns.slice(0, 8).map(renderTransaction).join('')
        : empty('No transactions found.'));
    } catch (error) {
      html('accountCards', errorBox(error.message));
      html('recentTxns', errorBox(error.message));
    }
  }

  async function loadAccounts() {
    html('accountsList', loading());

    try {
      const accounts = await api('/accounts');
      html('accountsList', accounts.length
        ? accounts.map(renderAccountRow).join('')
        : empty('No accounts found.'));
    } catch (error) {
      html('accountsList', errorBox(error.message));
    }
  }

  async function loadLoans() {
    html('loansList', loading());

    try {
      const loans = await api('/loans');
      html('loansList', loans.length
        ? loans.map(renderLoanRow).join('')
        : empty('No loans found.'));
    } catch (error) {
      html('loansList', errorBox(error.message));
    }
  }

  async function handleOpenAccount() {
    try {
      await api('/accounts', 'POST', {
        accountType: value('newAccType')
      });

      message('modalMsg', 'Account opened successfully.', 'success');
      closeModalById('openAccountModal');
      loadOverview();
      loadAccounts();
    } catch (error) {
      message('modalMsg', error.message, 'error');
    }
  }

  async function handleDeposit() {
    await runQuickAction('/transactions/deposit', 'Amount deposited.');
  }

  async function handleWithdraw() {
    await runQuickAction('/transactions/withdraw', 'Amount withdrawn.');
  }

  async function runQuickAction(path, successText) {
    const accountId = number('qaAccountId');
    const amount = number('qaAmount');

    if (!accountId || !amount || amount <= 0) {
      message('qaMsg', 'Enter a valid account ID and amount.', 'error');
      return;
    }

    try {
      await api(path, 'POST', { accountId, amount });
      message('qaMsg', successText, 'success');
      loadOverview();
      loadAccounts();
    } catch (error) {
      message('qaMsg', error.message, 'error');
    }
  }

  async function handleTransfer() {
    const payload = {
      fromAccountNumber: trimmed('txFrom'),
      toAccountNumber: trimmed('txTo'),
      amount: number('txAmount'),
      description: trimmed('txDesc')
    };

    if (!payload.fromAccountNumber || !payload.toAccountNumber || !payload.amount) {
      message('txMsg', 'Fill in the required fields.', 'error');
      return;
    }

    try {
      await api('/transactions/transfer', 'POST', payload);
      message('txMsg', 'Transfer completed.', 'success');
      loadOverview();
    } catch (error) {
      message('txMsg', error.message, 'error');
    }
  }

  async function handleLoanApply() {
    const payload = {
      accountId: number('lnAccount'),
      amount: number('lnAmount'),
      months: number('lnMonths')
    };

    if (!payload.accountId || !payload.amount || !payload.months) {
      message('lnMsg', 'Fill in all fields.', 'error');
      return;
    }

    try {
      await api('/loans/apply', 'POST', payload);
      message('lnMsg', 'Loan request submitted.', 'success');
      loadLoans();
    } catch (error) {
      message('lnMsg', error.message, 'error');
    }
  }

  async function handleReport() {
    const accountId = value('rptAccount');
    const from = value('rptFrom');
    const to = value('rptTo');

    if (!accountId || !from || !to) {
      message('rptMsg', 'Fill in all fields.', 'error');
      return;
    }

    try {
      const params = new URLSearchParams({ accountId, from, to });
      const response = await fetch(`${API_BASE}/reports/statement?${params}`, {
        credentials: 'same-origin'
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Report failed.');
      }

      const blob = await response.blob();
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);

      link.href = url;
      link.download = `statement_acc${accountId}_${from}_${to}.csv`;
      link.click();
      URL.revokeObjectURL(url);

      message('rptMsg', 'Statement downloaded.', 'success');
    } catch (error) {
      message('rptMsg', error.message, 'error');
    }
  }

  async function api(path, method = 'GET', body) {
    const options = {
      method,
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' }
    };

    if (body) options.body = JSON.stringify(body);

    const response = await fetch(`${API_BASE}${path}`, options);
    const data = await response.json();

    if (!response.ok) {
      if (response.status === 401) {
        sessionStorage.clear();
        window.location.href = 'index.html';
      }

      throw new Error(data.error || 'Request failed.');
    }

    return data;
  }

  function showModal(id) {
    document.getElementById(id).classList.add('open');
  }

  function closeModal(event, id) {
    if (event.target.id === id) closeModalById(id);
  }

  function closeModalById(id) {
    document.getElementById(id).classList.remove('open');
  }

  function setDefaultReportDates() {
    const today = new Date();
    const from = new Date(today);

    from.setDate(today.getDate() - 30);
    document.getElementById('rptTo').value = localDate(today);
    document.getElementById('rptFrom').value = localDate(from);
  }

  function renderAccountCard(account) {
    return `
      <div class="simple-card">
        <div class="label">${account.accountType}</div>
        <div class="amount">Rs. ${money(account.balance)}</div>
        <div class="muted">${account.accountNumber}</div>
        <div class="status">${account.status}</div>
      </div>
    `;
  }

  function renderAccountRow(account) {
    return `
      <div class="list-row">
        <div>
          <div>${account.accountType}</div>
          <div class="muted">${account.accountNumber} | ID: ${account.accountId}</div>
        </div>
        <div class="right">
          <div>Rs. ${money(account.balance)}</div>
          <div class="muted">${account.status}</div>
        </div>
      </div>
    `;
  }

  function renderLoanRow(loan) {
    return `
      <div class="list-row">
        <div>
          <div>Rs. ${money(loan.loanAmount)}</div>
          <div class="muted">${loan.tenureMonths} months | ID: ${loan.loanId}</div>
        </div>
        <div class="right">${loan.status}</div>
      </div>
    `;
  }

  function renderTransaction(txn) {
    return `
      <div class="list-row">
        <div>
          <div>${txn.txnType.replace('_', ' ')}</div>
          <div class="muted">${txn.referenceNo || 'No reference'}</div>
        </div>
        <div class="right">
          <div>Rs. ${money(txn.amount)}</div>
          <div class="muted">${txn.txnTimestamp || ''}</div>
        </div>
      </div>
    `;
  }

  function message(id, textValue, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = textValue;
    el.className = `msg ${type}`;
  }

  function loading() {
    return '<div class="placeholder">Loading...</div>';
  }

  function empty(textValue) {
    return `<div class="placeholder">${textValue}</div>`;
  }

  function errorBox(textValue) {
    return `<div class="msg error static-msg">${textValue}</div>`;
  }

  function money(value) {
    if (value === null || value === undefined) return '0.00';
    return Number.parseFloat(value).toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  }

  function localDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  function firstName(user) {
    return user.fullName?.split(' ')[0] || 'User';
  }

  function text(id, value) {
    document.getElementById(id).textContent = value;
  }

  function html(id, value) {
    document.getElementById(id).innerHTML = value;
  }

  function value(id) {
    return document.getElementById(id).value;
  }

  function trimmed(id) {
    return value(id).trim();
  }

  function number(id) {
    return Number.parseFloat(value(id));
  }
})();
