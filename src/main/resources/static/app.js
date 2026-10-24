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
    invalidAmount:
      "Please enter a valid positive amount (e.g. 5.99 or 1234.56)",
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
    invalidAmount:
      "Por favor, insira um valor positivo válido (ex: 5,99 ou 1234,56)",
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
  loadCategories();

  document
    .getElementById("expense-form")
    .addEventListener("submit", handleAddExpense);

  document.getElementById("filter-days").addEventListener("change", (e) => {
    const days = e.target.value;
    loadTransactions(days);
    loadCategoryTotals(days);
  });

  document
    .getElementById("edit-modal-close")
    ?.addEventListener("click", closeEditModal);
  document
    .getElementById("edit-form")
    ?.addEventListener("submit", handleEditSubmit);

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

  document
    .getElementById("show-new-category")
    ?.addEventListener("click", () => {
      document.getElementById("category").classList.add("hidden");
      document.getElementById("show-new-category").classList.add("hidden");
      document.getElementById("new-category-form").classList.remove("hidden");
      document.getElementById("new-category-name").focus();
    });

  document
    .getElementById("cancel-new-category")
    ?.addEventListener("click", () => {
      hideNewCategoryForm();
    });

  document
    .getElementById("add-category-btn")
    ?.addEventListener("click", async () => {
      const name = document.getElementById("new-category-name").value.trim();
      if (!name) {
        alert("Please enter a category name");
        return;
      }

      if (!authToken) {
        alert("You must be logged in to create a category");
        return;
      }

      try {
        const response = await fetch("/api/categories", {
          method: "POST",
          headers: getAuthHeaders(),
          body: JSON.stringify({ name }),
        });

        if (!response.ok) throw new Error("Failed to create category");

        const newCategory = await response.json();

        await loadCategories();
        document.getElementById("category").value = newCategory.name;

        hideNewCategoryForm();
      } catch (err) {
        alert("Error creating category: " + err.message);
      }
    });

  document
    .getElementById("edit-budgets-btn")
    ?.addEventListener("click", openBudgetsModal);
  document
    .getElementById("budgets-modal-close")
    ?.addEventListener("click", closeBudgetsModal);
  document
    .getElementById("add-budget-btn")
    ?.addEventListener("click", handleAddBudget);
  document.getElementById("budgets-modal")?.addEventListener("click", (e) => {
    if (e.target.id === "budgets-modal") closeBudgetsModal();
  });

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
    updateAmountPlaceholder();
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

  updateAmountPlaceholder();
  updateAuthUI();
}

function updateAmountPlaceholder() {
  const input = document.getElementById("amount");
  if (!input) return;

  input.placeholder = currentCurrency === "BRL" ? "0,00" : "0.00";
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
    loadBudgets(),
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
  const translatedLabels = labels.map((key) => {
    const upper = key.toUpperCase();
    return (
      dict.categories[upper] ||
      key.charAt(0).toUpperCase() + key.slice(1).toLowerCase()
    );
  });

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
    const page = await response.json();
    const expenses = page.content || [];

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

    expenses.forEach((exp) => {
      const tr = document.createElement("tr");
      tr.className = "hover:bg-slate-50 dark:hover:bg-slate-700/50 transition";
      const localizedCategory =
        dict.categories[exp.category] ||
        exp.category.charAt(0).toUpperCase() +
          exp.category.slice(1).toLowerCase();

      tr.innerHTML = `
        <td class="py-3 font-mono text-xs text-slate-500 dark:text-slate-400">${exp.date || "N/A"}</td>
        <td class="py-3 font-medium text-slate-800 dark:text-slate-200">${exp.description}</td>
        <td class="py-3"><span class="px-2 py-1 rounded text-xs font-semibold bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">${localizedCategory}</span></td>
        <td class="py-3 text-right font-bold text-slate-900 dark:text-slate-100">${formatCurrency(exp.amount)}</td>
        <td class="py-3 text-center space-x-2">
          <button
            data-id="${exp.id}"
            class="edit-btn text-indigo-600 hover:text-indigo-800 dark:text-indigo-400 dark:hover:text-indigo-300 font-medium text-xs px-2 py-1 rounded hover:bg-indigo-50 dark:hover:bg-indigo-950/40 transition"
          >
            Edit
          </button>
          <button
            onclick="deleteExpense(${exp.id})"
            class="text-rose-500 hover:text-rose-700 dark:text-rose-400 dark:hover:text-rose-300 font-medium text-xs px-2 py-1 rounded hover:bg-rose-50 dark:hover:bg-rose-950/40 transition"
          >
            ${dict.deleteBtn}
          </button>
        </td>
      `;
      tbody.appendChild(tr);
    });

    document.querySelectorAll(".edit-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = Number(btn.dataset.id);
        const expense = expenses.find((e) => e.id === id);
        if (expense) {
          openEditModal(expense);
        }
      });
    });
  } catch (err) {
    console.error("Error loading transactions:", err);
  }
}

async function handleAddExpense(e) {
  e.preventDefault();

  const dict = TRANSLATIONS[currentLang];

  const rawAmount = document.getElementById("amount").value;
  const amount = parseAmount(rawAmount);
  const category = document.getElementById("category").value;
  const description = document.getElementById("description").value;

  if (amount === null || amount <= 0) {
    alert(dict.invalidAmount);
    return;
  }

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

async function loadCategories() {
  try {
    const response = await fetch("/api/categories", {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to load categories");

    const categories = await response.json();
    populateCategorySelects(categories);
  } catch (err) {
    console.error("Error loading categories:", err);
  }

  updateNewCategoryVisibility();
}

function populateCategorySelects(categories) {
  const dict = TRANSLATIONS[currentLang];

  const selects = [
    document.getElementById("category"),
    document.getElementById("edit-category"),
  ];

  selects.forEach((select) => {
    if (!select) return;

    const currentValue = select.value;

    select.innerHTML = "";

    categories.forEach((cat) => {
      const option = document.createElement("option");
      option.value = cat.name;
      option.textContent =
        dict.categories[cat.name] ||
        cat.name.charAt(0).toUpperCase() + cat.name.slice(1).toLowerCase();
      select.appendChild(option);
    });

    if (currentValue) {
      select.value = currentValue;
    }
  });
}

function hideNewCategoryForm() {
  document.getElementById("new-category-form").classList.add("hidden");
  document.getElementById("category").classList.remove("hidden");
  document.getElementById("show-new-category").classList.remove("hidden");
  document.getElementById("new-category-name").value = "";
}

function updateNewCategoryVisibility() {
  const btn = document.getElementById("show-new-category");
  if (!btn) return;
  btn.classList.toggle("hidden", !authToken);
}

async function loadBudgets() {
  const list = document.getElementById("budgets-list");
  if (!list) return;

  if (!authToken) {
    list.innerHTML = `
      <p class="text-xs text-slate-400 dark:text-slate-500 text-center py-4">
        No budgets set yet
      </p>`;
    return;
  }

  try {
    const response = await fetch("/api/budgets", {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to load budgets");

    const budgets = await response.json();
    renderBudgets(budgets);
  } catch (err) {
    console.error("Error loading budgets:", err);
  }
}

function renderBudgets(budgets) {
  const list = document.getElementById("budgets-list");
  const dict = TRANSLATIONS[currentLang];

  list.innerHTML = "";

  if (!budgets || budgets.length === 0) {
    list.innerHTML = `
      <p class="text-xs text-slate-400 dark:text-slate-500 text-center py-4">
        No budgets set yet
      </p>`;
    return;
  }

  budgets.forEach((b) => {
    const categoryName =
      dict.categories[b.category] ||
      b.category.charAt(0).toUpperCase() + b.category.slice(1).toLowerCase();

    const pct = Math.min(b.percentage, 100);
    let barColor = "bg-emerald-500";
    if (b.percentage >= 80) barColor = "bg-amber-500";
    if (b.percentage >= 100) barColor = "bg-rose-500";

    const row = document.createElement("div");
    row.className = "flex flex-col gap-0.5";
    row.innerHTML = `
      <div class="flex justify-between text-xs">
        <span class="font-medium text-slate-700 dark:text-slate-300">${categoryName}</span>
        <span class="text-slate-500 dark:text-slate-400">
          ${formatCurrency(b.spent)} / ${formatCurrency(b.limit)}
        </span>
      </div>
      <div class="w-full bg-slate-100 dark:bg-slate-700 rounded-full h-1.5">
        <div class="${barColor} h-1.5 rounded-full" style="width: ${pct}%"></div>
      </div>
    `;
    list.appendChild(row);
  });
}

function formatCurrency(amount) {
  const locale = currentCurrency === "BRL" ? "pt-BR" : "en-US";
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: currentCurrency,
  }).format(amount);
}

/**
 * Parses a user-entered amount string that may use
 * either comma or dot as decimal separator and may contain
 * thousand separators (depending on the selected currency).
 * Returns a number or null if invalid.
 */
function parseAmount(value) {
  if (!value || typeof value !== "string") return null;

  let cleaned = value.trim().replace(/[^\d.,]/g, "");
  if (cleaned === "") return null;

  const isBR = currentCurrency === "BRL";

  if (isBR) {
    // Brazilian format: 1.234,56
    cleaned = cleaned.replace(/\./g, "");
    cleaned = cleaned.replace(",", ".");
  } else {
    // US format: 1,234.56
    cleaned = cleaned.replace(/,/g, "");
  }

  const number = parseFloat(cleaned);
  return isNaN(number) ? null : number;
}

// ==================
// Edit Expense Modal
// ==================

function openEditModal(expense) {
  document.getElementById("edit-id").value = expense.id;
  document.getElementById("edit-amount").value = expense.amount;
  document.getElementById("edit-category").value = expense.category;
  document.getElementById("edit-description").value = expense.description;
  document.getElementById("edit-date").value = expense.date;

  const modal = document.getElementById("edit-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

function closeEditModal() {
  const modal = document.getElementById("edit-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}

async function handleEditSubmit(e) {
  e.preventDefault();

  const id = document.getElementById("edit-id").value;
  const rawAmount = document.getElementById("edit-amount").value;
  const amount = parseAmount(rawAmount);
  const category = document.getElementById("edit-category").value;
  const description = document.getElementById("edit-description").value;
  const date = document.getElementById("edit-date").value;

  if (amount === null || amount <= 0) {
    alert(TRANSLATIONS[currentLang].invalidAmount);
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      body: JSON.stringify({ amount, category, description, date }),
    });

    if (!response.ok) throw new Error("Failed to update expense");

    closeEditModal();
    await initDashboard();
  } catch (err) {
    alert("Error updating expense: " + err.message);
  }
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

  updateNewCategoryVisibility();
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
      await loadCategories();
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
  loadCategories();
  initDashboard();
}

// =============================
// Add/Edit Budget Modal & Logic
// =============================

function openBudgetsModal() {
  if (!authToken) {
    alert("You must be logged in to manage budgets");
    return;
  }

  const modal = document.getElementById("budgets-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");

  populateBudgetCategorySelect();
  loadBudgetsForModal();
}

function closeBudgetsModal() {
  const modal = document.getElementById("budgets-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}

async function populateBudgetCategorySelect() {
  const select = document.getElementById("budget-category");
  if (!select) return;

  try {
    const response = await fetch("/api/categories", {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to load categories");

    const categories = await response.json();
    const dict = TRANSLATIONS[currentLang];

    select.innerHTML = "";
    categories.forEach((cat) => {
      const option = document.createElement("option");
      option.value = cat.name;
      option.textContent =
        dict.categories[cat.name] ||
        cat.name.charAt(0).toUpperCase() + cat.name.slice(1).toLowerCase();
      select.appendChild(option);
    });
  } catch (err) {
    console.error(err);
  }
}

async function loadBudgetsForModal() {
  const container = document.getElementById("budgets-modal-list");
  if (!container) return;

  try {
    const response = await fetch("/api/budgets", {
      headers: getAuthHeaders(),
    });
    if (!response.ok) throw new Error("Failed to load budgets");

    const budgets = await response.json();
    renderBudgetsInModal(budgets);
  } catch (err) {
    console.error(err);
    container.innerHTML = `<p class="text-sm text-slate-400">Could not load budgets</p>`;
  }
}

function renderBudgetsInModal(budgets) {
  const container = document.getElementById("budgets-modal-list");
  const dict = TRANSLATIONS[currentLang];

  container.innerHTML = "";

  if (!budgets || budgets.length === 0) {
    container.innerHTML = `<p class="text-sm text-slate-400 dark:text-slate-500">No budgets yet</p>`;
    return;
  }

  budgets.forEach((b) => {
    const categoryName =
      dict.categories[b.category] ||
      b.category.charAt(0).toUpperCase() + b.category.slice(1).toLowerCase();

    const row = document.createElement("div");
    row.className = "flex items-center gap-2";
    row.innerHTML = `
      <span class="flex-1 text-sm font-medium text-slate-700 dark:text-slate-300">
        ${categoryName}
      </span>
      <input
        type="text"
        value="${b.limit}"
        data-id="${b.id}"
        data-category="${b.category}"
        class="budget-amount-input w-24 px-2 py-1 text-sm border border-slate-300 dark:border-slate-600 bg-white dark:bg-slate-700 rounded-lg text-right"
      />
      <button
        type="button"
        data-id="${b.id}"
        class="delete-budget-btn text-rose-500 hover:text-rose-700 text-sm px-1"
        title="Delete"
      >
        ✕
      </button>
    `;
    container.appendChild(row);
  });

  // Listeners
  container.querySelectorAll(".budget-amount-input").forEach((input) => {
    input.addEventListener("change", handleBudgetAmountChange);
  });

  container.querySelectorAll(".delete-budget-btn").forEach((btn) => {
    btn.addEventListener("click", handleDeleteBudget);
  });
}

async function handleBudgetAmountChange(e) {
  const input = e.target;
  const amount = parseAmount(input.value);
  const category = input.dataset.category;

  if (amount === null || amount <= 0) {
    alert("Please enter a valid positive amount");
    return;
  }

  try {
    const response = await fetch("/api/budgets", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ category, amount }),
    });

    if (!response.ok) throw new Error("Failed to update budget");

    await loadBudgets();
    await loadBudgetsForModal();
  } catch (err) {
    alert("Error updating budget: " + err.message);
  }
}

async function handleDeleteBudget(e) {
  const id = e.target.dataset.id;
  if (!confirm("Delete this budget?")) return;

  try {
    const response = await fetch(`/api/budgets/${id}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
    });

    if (!response.ok) throw new Error("Failed to delete budget");

    await loadBudgets();
    await loadBudgetsForModal();
  } catch (err) {
    console.error("Error deleting budget: " + err.message);
  }
}

async function handleAddBudget() {
  const category = document.getElementById("budget-category").value;
  const rawAmount = document.getElementById("budget-amount").value;
  const amount = parseAmount(rawAmount);

  if (amount === null || amount <= 0) {
    alert("Please enter a valid positive amount");
    return;
  }

  try {
    const response = await fetch("/api/budgets", {
      method: "POST",
      headers: getAuthHeaders(),
      body: JSON.stringify({ category, amount }),
    });

    if (!response.ok) throw new Error("Failed to create budget");

    document.getElementById("budget-amount").value = "";
    await loadBudgets();
    await loadBudgetsForModal();
  } catch (err) {
    alert("Error creating budget: " + err.message);
  }
}
