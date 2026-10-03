const API_BASE_URL = "/api/expenses";

let categoryChart = null;

const CATEGORY_COLORS = {
  FOOD: "#10b981",
  TRANSPORTATION: "#3b82f6",
  UTILITIES: "#f59e0b",
  HOUSING: "#6366f1",
  ENTERTAINMENT: "#ec4899",
  HEALTHCARE: "#ef4444",
  SHOPPING: "#8b5cf6",
  OTHER: "#64748b",
};

const TRANSLATIONS = {
  en: {
    title: "Expense Tracker",
    subtitle: "Personal spending analytics and history",
    apiConnected: "API Connected",
    currentMonth: "Current Month",
    previousMonth: "Previous Month",
    vsLastMonth: "vs. Last Month",
    addExpense: "Add Expense",
    amountLabel: "Amount",
    categoryLabel: "Category",
    descriptionLabel: "Description",
    descriptionPlaceholder: "e.g. Groceries",
    saveExpense: "Save Expense",
    spendingByCategory: "Spending by Category",
    recentTransactions: "Recent Transactions",
    filterAllTime: "All Time",
    filter7Days: "Last 7 Days",
    filter14Days: "Last 14 Days",
    filter30Days: "Last 30 Days",
    thDate: "Date",
    thDescription: "Description",
    thCategory: "Category",
    thAmount: "Amount",
    thAction: "Action",
    noTransactions: "No transactions found",
    deleteBtn: "Delete",
    deleteConfirm: "Are you sure you want to delete this expense?",
    categories: {
      FOOD: "Food",
      TRANSPORTATION: "Transportation",
      UTILITIES: "Utilities",
      HOUSING: "Housing",
      ENTERTAINMENT: "Entertainment",
      HEALTHCARE: "Healthcare",
      SHOPPING: "Shopping",
      OTHER: "Other",
    },
    login: "Log In",
    register: "Register",
    usernameLabel: "Username",
    passwordLabel: "Password",
    loginButton: "Log In",
    registerButton: "Register",
    authButton: "Log In / Register",
    logoutSuffix: "(Logout)",
    accountCreated: "Account created! Please log in.",
    invalidCredentials: "Invalid username or password",
    usernameTaken: "Username is already taken",
    registrationFailed: "Registration failed",
  },
  pt: {
    title: "Controle de Despesas",
    subtitle: "Análise e histórico de gastos pessoais",
    apiConnected: "API Conectada",
    currentMonth: "Mês Atual",
    previousMonth: "Mês Anterior",
    vsLastMonth: "vs. Mês Anterior",
    addExpense: "Adicionar Despesa",
    amountLabel: "Valor",
    categoryLabel: "Categoria",
    descriptionLabel: "Descrição",
    descriptionPlaceholder: "ex: Supermercado",
    saveExpense: "Salvar Despesa",
    spendingByCategory: "Gastos por Categoria",
    recentTransactions: "Transações Recentes",
    filterAllTime: "Todo o período",
    filter7Days: "Últimos 7 dias",
    filter14Days: "Últimos 14 dias",
    filter30Days: "Últimos 30 dias",
    thDate: "Data",
    thDescription: "Descrição",
    thCategory: "Categoria",
    thAmount: "Valor",
    thAction: "Ação",
    noTransactions: "Nenhuma transação encontrada",
    deleteBtn: "Excluir",
    deleteConfirm: "Tem certeza de que deseja excluir esta despesa?",
    categories: {
      FOOD: "Alimentação",
      TRANSPORTATION: "Transporte",
      UTILITIES: "Contas / Serviços",
      HOUSING: "Moradia",
      ENTERTAINMENT: "Lazer",
      HEALTHCARE: "Saúde",
      SHOPPING: "Compras",
      OTHER: "Outros",
    },
    login: "Entrar",
    register: "Registrar",
    usernameLabel: "Nome de usuário",
    passwordLabel: "Senha",
    loginButton: "Entrar",
    registerButton: "Registrar",
    authButton: "Entrar / Registrar",
    logoutSuffix: "(Sair)",
    accountCreated: "Conta criada! Por favor, faça login.",
    invalidCredentials: "Usuário ou senha inválidos",
    usernameTaken: "Nome de usuário já está em uso",
    registrationFailed: "Falha ao criar conta",
  },
};

let currentCurrency = localStorage.getItem("preferred_currency") || "USD";
let currentLang = localStorage.getItem("preferred_lang") || "en";
if (!TRANSLATIONS[currentLang]) {
  currentLang = "en";
}

let currentTheme = localStorage.getItem("preferred_theme");
if (!currentTheme) {
  currentTheme = window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

let authToken = localStorage.getItem("jwt_token") || null;
let currentUser = localStorage.getItem("current_user") || null;

document.addEventListener("DOMContentLoaded", () => {
  applyTheme();
  setupSettingsUI();
  applyLanguage();
  initDashboard();

  document
    .getElementById("expense-form")
    .addEventListener("submit", handleAddExpense);

  document.getElementById("filter-days").addEventListener("change", (e) => {
    const days = e.target.value;
    loadTransactions(days);
    loadCategoryTotals(days);
  });

  const authButton = document.getElementById("auth-button");
  if (authButton) {
    authButton.addEventListener("click", () => {
      if (authToken) {
        logout();
      } else {
        openAuthModal();
      }
    });
  }

  document
    .getElementById("auth-modal-close")
    ?.addEventListener("click", closeAuthModal);

  document.getElementById("auth-modal")?.addEventListener("click", (e) => {
    if (e.target.id === "auth-modal") closeAuthModal();
  });

  document
    .getElementById("tab-login")
    ?.addEventListener("click", () => switchAuthTab("login"));
  document
    .getElementById("tab-register")
    ?.addEventListener("click", () => switchAuthTab("register"));

  document
    .getElementById("auth-form")
    ?.addEventListener("submit", handleAuthSubmit);

  updateAuthUI();
});

function setupSettingsUI() {
  const currencySelect = document.getElementById("currency-select");
  const langSelect = document.getElementById("language-select");
  const themeToggleBtn = document.getElementById("theme-toggle");

  currencySelect.value = currentCurrency;
  langSelect.value = currentLang;

  currencySelect.addEventListener("change", (e) => {
    currentCurrency = e.target.value;
    localStorage.setItem("preferred_currency", currentCurrency);
    initDashboard();
  });

  langSelect.addEventListener("change", (e) => {
    currentLang = e.target.value;
    localStorage.setItem("preferred_lang", currentLang);
    applyLanguage();
    initDashboard();
  });

  if (themeToggleBtn) {
    themeToggleBtn.addEventListener("click", () => {
      currentTheme = currentTheme === "dark" ? "light" : "dark";
      localStorage.setItem("preferred_theme", currentTheme);
      applyTheme();

      const currentDays = document.getElementById("filter-days").value;
      loadCategoryTotals(currentDays);
    });
  }
}

function applyTheme() {
  const iconElem = document.getElementById("theme-toggle-icon");
  if (currentTheme === "dark") {
    document.documentElement.classList.add("dark");
    if (iconElem) iconElem.textContent = "☀️";
  } else {
    document.documentElement.classList.remove("dark");
    if (iconElem) iconElem.textContent = "🌙";
  }
}

function applyLanguage() {
  document.documentElement.lang = currentLang;

  const dict = TRANSLATIONS[currentLang];

  document.querySelectorAll("[data-i18n]").forEach((elem) => {
    const key = elem.getAttribute("data-i18n");
    if (dict[key]) {
      elem.innerText = dict[key];
    }
  });

  const descInput = document.getElementById("description");
  if (descInput) {
    descInput.placeholder = dict.descriptionPlaceholder;
  }

  const categorySelect = document.getElementById("category");
  if (categorySelect) {
    Array.from(categorySelect.options).forEach((opt) => {
      if (dict.categories[opt.value]) {
        opt.text = dict.categories[opt.value];
      }
    });
  }

  updateAuthUI();
}

function getAuthHeaders() {
  const headers = { "Content-Type": "application/json" };
  if (authToken) {
    headers["Authorization"] = `Bearer ${authToken}`;
  }
  return headers;
}

async function initDashboard() {
  const currentDays = document.getElementById("filter-days").value;
  await Promise.all([
    loadMonthlyReport(),
    loadCategoryTotals(currentDays),
    loadTransactions(currentDays),
  ]);
}

async function loadMonthlyReport() {
  try {
    const response = await fetch(`${API_BASE_URL}/monthly-report`, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to fetch monthly report");
    const report = await response.json();

    document.getElementById("current-month-total").innerText = formatCurrency(
      report.currentTotal || 0,
    );
    document.getElementById("previous-month-total").innerText = formatCurrency(
      report.previousTotal || 0,
    );

    const variance = report.percentageChange ?? 0;
    const varianceElem = document.getElementById("month-variance");

    let varianceColor = "text-slate-500 dark:text-slate-400";
    if (variance > 0) varianceColor = "text-amber-600 dark:text-amber-400";
    if (variance < 0) varianceColor = "text-emerald-600 dark:text-emerald-400";
    const sign = variance > 0 ? "+" : "";

    varianceElem.innerText = `${sign}${variance.toFixed(1)}%`;
    varianceElem.className = `text-3xl font-extrabold mt-2 ${varianceColor}`;
  } catch (err) {
    console.error("Error loading monthly report: ", err);
  }
}

async function loadCategoryTotals(days = "") {
  try {
    const url = days
      ? `${API_BASE_URL}/category-totals?days=${days}`
      : `${API_BASE_URL}/category-totals`;
    const response = await fetch(url, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to fetch category totals");
    const totals = await response.json();

    const labels = Object.keys(totals);
    const data = Object.values(totals);

    renderChart(labels, data);
  } catch (err) {
    console.error("Error loading category totals:", err);
  }
}

function renderChart(labels, data) {
  const ctx = document.getElementById("category-chart").getContext("2d");

  if (categoryChart) {
    categoryChart.destroy();
  }

  const isDark = currentTheme === "dark";
  const legendTextColor = isDark ? "#cbd5e1" : "#475569";
  const sliceBorderColor = isDark ? "#1e293b" : "#ffffff";

  const dict = TRANSLATIONS[currentLang];
  const translatedLabels = labels.map(
    (key) => dict.categories[key.toUpperCase()] || key,
  );

  const backgroundColors = labels.map(
    (label) => CATEGORY_COLORS[label.toUpperCase()] || "#94a3b8",
  );

  categoryChart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: translatedLabels,
      datasets: [
        {
          data: data,
          backgroundColor: backgroundColors,
          borderColor: sliceBorderColor,
          borderWidth: 2,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom",
          labels: {
            color: legendTextColor,
            padding: 16,
            usePointStyle: true,
            pointStyle: "circle",
          },
        },
        tooltip: {
          backgroundColor: isDark ? "#0f172a" : "#ffffff",
          titleColor: isDark ? "#f8fafc" : "#0f172a",
          bodyColor: isDark ? "#e2e8f0" : "#334155",
          borderColor: isDark ? "#334155" : "#e2e8f0",
          borderWidth: 1,
          padding: 10,
          callbacks: {
            label: (context) => ` ${formatCurrency(context.raw)}`,
          },
        },
      },
    },
  });
}

async function loadTransactions(days = "") {
  try {
    const url = days ? `${API_BASE_URL}?days=${days}` : API_BASE_URL;
    const response = await fetch(url, {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to fetch transactions");
    const expenses = await response.json();

    const tbody = document.getElementById("expense-table-body");
    const dict = TRANSLATIONS[currentLang];
    tbody.innerHTML = "";

    if (expenses.length === 0) {
      tbody.innerHTML = `
        <tr>
            <td colspan="5" class="py-4 text-center text-slate-400 dark:text-slate-500">${dict.noTransactions}</td>
        </tr>`;
      return;
    }

    const reversedExpenses = [...expenses].reverse();

    reversedExpenses.forEach((exp) => {
      const tr = document.createElement("tr");
      tr.className = "hover:bg-slate-50 dark:hover:bg-slate-700/50 transition";
      const localizedCategory = dict.categories[exp.category] || exp.category;

      tr.innerHTML = `
        <td class="py-3 font-mono text-xs text-slate-500 dark:text-slate-400">${exp.date || "N/A"}</td>
        <td class="py-3 font-medium text-slate-800 dark:text-slate-200">${exp.description}</td>
        <td class="py-3"><span class="px-2 py-1 rounded text-xs font-semibold bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">${localizedCategory}</span></td>
        <td class="py-3 text-right font-bold text-slate-900 dark:text-slate-100">${formatCurrency(exp.amount)}</td>
        <td class="py-3 text-center">
          <button onclick="deleteExpense(${exp.id})" class="text-rose-500 hover:text-rose-700 dark:text-rose-400 dark:hover:text-rose-300 font-medium text-xs px-2 py-1 rounded hover:bg-rose-50 dark:hover:bg-rose-950/40 transition">
            ${dict.deleteBtn}
          </button>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("Error loading transactions:", err);
  }
}

async function handleAddExpense(e) {
  e.preventDefault();

  const amount = parseFloat(document.getElementById("amount").value);
  const category = document.getElementById("category").value;
  const description = document.getElementById("description").value;

  try {
    const response = await fetch(API_BASE_URL, {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ amount, category, description }),
    });

    if (!response.ok) throw new Error("Failed to create expense");

    document.getElementById("expense-form").reset();
    await initDashboard();
  } catch (err) {
    alert("Error adding expense: " + err.message);
  }
}

async function deleteExpense(id) {
  const dict = TRANSLATIONS[currentLang];
  if (!confirm(dict.deleteConfirm)) return;

  try {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error("Failed to delete expense");

    await initDashboard();
  } catch (err) {
    alert("Error deleting expense: " + err.message);
  }
}

function formatCurrency(amount) {
  const locale = currentCurrency === "BRL" ? "pt-BR" : "en-US";
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: currentCurrency,
  }).format(amount);
}

// =========================
// Authentication UI & Logic
// =========================

function updateAuthUI() {
  const authButton = document.getElementById("auth-button");
  if (!authButton) return;

  const dict = TRANSLATIONS[currentLang];

  if (authToken && currentUser) {
    authButton.textContent = `${currentUser} ${dict.logoutSuffix}`;
    authButton.classList.remove("bg-indigo-600", "hover:bg-indigo-700");
    authButton.classList.add("bg-slate-600", "hover:bg-slate-700");
  } else {
    authButton.textContent = dict.authButton;
    authButton.classList.remove(
      "bg-slate-600",
      "hover:bg-slate-700",
      "dark:bg-slate-500",
      "dark:hover:bg-slate-600",
    );
    authButton.classList.add("bg-indigo-600", "hover:bg-indigo-700");
  }
}

function openAuthModal() {
  const modal = document.getElementById("auth-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
  document.getElementById("auth-error").classList.add("hidden");
  document.getElementById("auth-form").reset();
  switchAuthTab("login");
}

function closeAuthModal() {
  const modal = document.getElementById("auth-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}

function switchAuthTab(tab) {
  const loginTab = document.getElementById("tab-login");
  const registerTab = document.getElementById("tab-register");
  const title = document.getElementById("auth-modal-title");
  const submitBtn = document.getElementById("auth-submit");
  const dict = TRANSLATIONS[currentLang];

  if (tab === "login") {
    loginTab.classList.add(
      "text-indigo-600",
      "dark:text-indigo-400",
      "border-b-2",
      "border-indigo-600",
      "dark:border-indigo-400",
    );
    loginTab.classList.remove("text-slate-500", "dark:text-slate-400");
    registerTab.classList.remove(
      "text-indigo-600",
      "dark:text-indigo-400",
      "border-b-2",
      "border-indigo-600",
      "dark:border-indigo-400",
    );
    registerTab.classList.add("text-slate-500", "dark:text-slate-400");
    title.textContent = dict.login;
    submitBtn.textContent = dict.loginButton;
  } else {
    registerTab.classList.add(
      "text-indigo-600",
      "dark:text-indigo-400",
      "border-b-2",
      "border-indigo-600",
      "dark:border-indigo-400",
    );
    registerTab.classList.remove("text-slate-500", "dark:text-slate-400");
    loginTab.classList.remove(
      "text-indigo-600",
      "dark:text-indigo-400",
      "border-b-2",
      "border-indigo-600",
      "dark:border-indigo-400",
    );
    loginTab.classList.add("text-slate-500", "dark:text-slate-400");
    title.textContent = dict.register;
    submitBtn.textContent = dict.registerButton;
  }
}

async function handleAuthSubmit(e) {
  e.preventDefault();

  const username = document.getElementById("auth-username").value.trim();
  const password = document.getElementById("auth-password").value;
  const errorElem = document.getElementById("auth-error");
  const dict = TRANSLATIONS[currentLang];
  const isLogin =
    document.getElementById("auth-modal-title").textContent === dict.login;

  errorElem.classList.add("hidden");

  try {
    if (isLogin) {
      // --- LOGIN ---
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        throw new Error(dict.invalidCredentials);
      }

      const data = await response.json();
      authToken = data.token;
      currentUser = data.username;

      localStorage.setItem("jwt_token", authToken);
      localStorage.setItem("current_user", currentUser);

      closeAuthModal();
      updateAuthUI();
      await initDashboard();
    } else {
      // --- REGISTER ---
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      if (response.status === 409) {
        throw new Error(dict.usernameTaken);
      }
      if (!response.ok) {
        throw new Error(dict.registrationFailed);
      }

      switchAuthTab("login");
      errorElem.textContent = dict.accountCreated;
      errorElem.classList.remove("hidden");
      errorElem.classList.remove("text-rose-600", "dark:text-rose-400");
      errorElem.classList.add("text-emerald-600", "dark:text-emerald-400");
    }
  } catch (err) {
    errorElem.textContent = err.message;
    errorElem.classList.remove("hidden");
    errorElem.classList.add("text-rose-600", "dark:text-rose-400");
    errorElem.classList.remove("text-emerald-600", "dark:text-emerald-400");
  }
}

function logout() {
  authToken = null;
  currentUser = null;
  localStorage.removeItem("jwt_token");
  localStorage.removeItem("current_user");
  updateAuthUI();
  initDashboard();
}
