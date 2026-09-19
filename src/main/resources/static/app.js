const API_BASE_URL = "/api/expenses";

let categoryChart = null;

document.addEventListener("DOMContentLoaded", () => {
  initDashboard();

  document
    .getElementById("expense-form")
    .addEventListener("submit", handleAddExpense);

  document.getElementById("filter-days").addEventListener("change", (e) => {
    loadTransactions(e.target.value);
  });
});

async function initDashboard() {
  await Promise.all([
    loadMonthlyReport(),
    loadCategoryTotals(),
    loadTransactions(document.getElementById("filter-days").value),
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

    const variance = report.percentageChange ?? report.variance ?? 0;
    const varianceElem = document.getElementById("month-variance");

    let varianceColor = "text-slate-500";
    if (variance > 0) varianceColor = "text-amber-600";
    if (variance > 0) varianceColor = "text-emerald-600";
    const sign = variance > 0 ? "+" : "";

    varianceElem.innerText = `${sign}${variance.toFixed(1)}%`;
    varianceElem.className = `text-3xl font-extrabold mt-2 ${varianceColor}`;
  } catch (err) {
    console.error("Error loading monthly report: ", err);
  }
}

async function loadCategoryTotals() {
  try {
    const response = await fetch(`${API_BASE_URL}/category-totals`);
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

  categoryChart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: labels.map((l) => l.charAt(0) + l.slice(1).toLowerCase()),
      datasets: [
        {
          data: data,
          backgroundColor: [
            "#6366f1",
            "#10b981",
            "#f59e0b",
            "#ef4444",
            "#8b5cf6",
            "#ec4899",
            "#14b8a6",
            "#64748b",
          ],
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: "bottom" },
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
    tbody.innerHTML = "";

    if (expenses.length === 0) {
      tbody.innerHTML = `
        <tr>
            <td colspan="5" class="py-4 text-center text-slate-400">No transactions found</td>
        </tr>`;
      return;
    }

    const reversedExpenses = [...expenses].reverse();

    reversedExpenses.forEach((exp) => {
      const tr = document.createElement("tr");
      tr.className = "hover:bg-slate-50 transition";
      tr.innerHTML = `
        <td class="py-3 font-mono text-xs text-slate-500">${exp.date || "N/A"}</td>
        <td class="py-3"><span class="px-2 py-1 rounded text-xs font-semibold bg-slate-100 text-slate-700">${exp.category}</span></td>
        <td class="py-3 font-medium text-slate-800">${exp.description}</td>
        <td class="py-3 text-right font-bold text-slate-900">${formatCurrency(exp.amount)}</td>
        <td class="py-3 text-center">
          <button onclick="deleteExpense(${exp.id})" class="text-rose-500 hover:text-rose-700 font-medium text-xs px-2 py-1 rounded hover:bg-rose-50 transition">
            Delete
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
  if (!confirm("Are you sure you want to delete this expense?")) return;

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
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(amount);
}
