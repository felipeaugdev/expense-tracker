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
  },
};

let currentCurrency = localStorage.getItem("preferred_currency") || "USD";
let currentLang = localStorage.getItem("preferred_lang") || "en";
if (!TRANSLATIONS[currentLang]) {
  currentLang = "en";
}

document.addEventListener("DOMContentLoaded", () => {
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
});

function setupSettingsUI() {
  const currencySelect = document.getElementById("currency-select");
  const langSelect = document.getElementById("language-select");

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
    const response = await fetch(`${API_BASE_URL}/monthly-report`);
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

    let varianceColor = "text-slate-500";
    if (variance > 0) varianceColor = "text-amber-600";
    if (variance < 0) varianceColor = "text-emerald-600";
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
    const response = await fetch(url);
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
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: "bottom" },
        tooltip: {
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
    const response = await fetch(url);
    if (!response.ok) throw new Error("Failed to fetch transactions");
    const expenses = await response.json();

    const tbody = document.getElementById("expense-table-body");
    const dict = TRANSLATIONS[currentLang];
    tbody.innerHTML = "";

    if (expenses.length === 0) {
      tbody.innerHTML = `
        <tr>
            <td colspan="5" class="py-4 text-center text-slate-400">${dict.noTransactions}</td>
        </tr>`;
      return;
    }

    const reversedExpenses = [...expenses].reverse();

    reversedExpenses.forEach((exp) => {
      const tr = document.createElement("tr");
      tr.className = "hover:bg-slate-50 transition";
      const localizedCategory = dict.categories[exp.category] || exp.category;

      tr.innerHTML = `
        <td class="py-3 font-mono text-xs text-slate-500">${exp.date || "N/A"}</td>
        <td class="py-3 font-medium text-slate-800">${exp.description}</td>
        <td class="py-3"><span class="px-2 py-1 rounded text-xs font-semibold bg-slate-100 text-slate-700">${localizedCategory}</span></td>
        <td class="py-3 text-right font-bold text-slate-900">${formatCurrency(exp.amount)}</td>
        <td class="py-3 text-center">
          <button onclick="deleteExpense(${exp.id})" class="text-rose-500 hover:text-rose-700 font-medium text-xs px-2 py-1 rounded hover:bg-rose-50 transition">
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
      headers: { "Content-Type": "application/json" },
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
