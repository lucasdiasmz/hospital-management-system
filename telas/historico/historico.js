"use strict";

const patients = {
  joao: {
    name: "João Silva", cpf: "123.456.789-00", birth: "01/01/1980",
    consultations: [
      { date: "10/10/2025", professional: "Dr. Rafael Costa", reason: "Check-up", notes: "Sem observações" },
      { date: "15/09/2025", professional: "Dra. Fernanda Lima", reason: "Dor de cabeça", notes: "Prescrição de analgésico" },
      { date: "20/08/2025", professional: "Dr. Marcos Alves", reason: "Retorno", notes: "Evolução positiva" }
    ],
    admissions: [{ entry: "05/10/2025", discharge: "12/10/2025", room: "203", notes: "Apendicite" }],
    relevant: "Nenhuma informação adicional registrada."
  },
  maria: {
    name: "Maria Oliveira", cpf: "987.654.321-00", birth: "12/04/1975",
    consultations: [
      { date: "10/10/2025", professional: "Dra. Fernanda Lima", reason: "Avaliação cardiológica", notes: "Solicitados exames complementares" },
      { date: "02/07/2025", professional: "Dr. Rafael Costa", reason: "Consulta de rotina", notes: "Sem observações" }
    ],
    admissions: [{ entry: "05/10/2025", discharge: "Em andamento", room: "101", notes: "Observação clínica" }],
    relevant: "Alergia registrada: dipirona."
  },
  carlos: {
    name: "Carlos Santos", cpf: "456.789.123-00", birth: "23/11/1990",
    consultations: [{ date: "11/10/2025", professional: "Dr. Marcos Alves", reason: "Dor no joelho", notes: "Encaminhado para radiografia" }],
    admissions: [], relevant: "Nenhuma informação adicional registrada."
  }
};

const $ = (selector) => document.querySelector(selector);
const elements = {
  patientSelect: $("#patientSelect"), patientSearch: $("#patientSearch"),
  name: $("#patientName"), cpf: $("#patientCpf"), birth: $("#patientBirth"), initials: $("#patientInitials"),
  consultationsBody: $("#consultationsBody"), consultationsMobile: $("#consultationsMobile"),
  admissionsBody: $("#admissionsBody"), admissionsMobile: $("#admissionsMobile"),
  consultationsCount: $("#consultationsCount"), admissionsCount: $("#admissionsCount"), relevant: $("#relevantInfo"),
  sidebar: $("#sidebar"), overlay: $("#sidebarOverlay"), menuButton: $("#menuButton"), toast: $("#toast")
};

function initials(name) {
  return name.split(" ").slice(0, 2).map(part => part[0]).join("").toUpperCase();
}
function countLabel(total) { return `${total} ${total === 1 ? "registro" : "registros"}`; }
function safe(value) {
  const node = document.createElement("span"); node.textContent = value; return node.innerHTML;
}
function emptyRow(columns, message) { return `<tr><td colspan="${columns}" class="empty-state">${message}</td></tr>`; }
function mobileCards(items, fields, emptyMessage) {
  if (!items.length) return `<div class="empty-state">${emptyMessage}</div>`;
  return items.map(item => `<article class="mobile-record"><dl>${fields.map(field => `<div><dt>${field.label}</dt><dd>${safe(item[field.key])}</dd></div>`).join("")}</dl></article>`).join("");
}
function renderPatient(key) {
  const patient = patients[key]; if (!patient) return;
  elements.name.textContent = patient.name; elements.cpf.textContent = patient.cpf;
  elements.birth.textContent = patient.birth; elements.initials.textContent = initials(patient.name);
  elements.relevant.textContent = patient.relevant;
  elements.consultationsCount.textContent = countLabel(patient.consultations.length);
  elements.admissionsCount.textContent = countLabel(patient.admissions.length);
  elements.consultationsBody.innerHTML = patient.consultations.length
    ? patient.consultations.map(c => `<tr><td>${safe(c.date)}</td><td>${safe(c.professional)}</td><td>${safe(c.reason)}</td><td>${safe(c.notes)}</td></tr>`).join("")
    : emptyRow(4, "Nenhuma consulta registrada.");
  elements.admissionsBody.innerHTML = patient.admissions.length
    ? patient.admissions.map(a => `<tr><td>${safe(a.entry)}</td><td>${safe(a.discharge)}</td><td>${safe(a.room)}</td><td>${safe(a.notes)}</td></tr>`).join("")
    : emptyRow(4, "Nenhuma internação registrada.");
  elements.consultationsMobile.innerHTML = mobileCards(patient.consultations, [
    {label:"Data", key:"date"}, {label:"Profissional", key:"professional"}, {label:"Motivo", key:"reason"}, {label:"Observações", key:"notes"}
  ], "Nenhuma consulta registrada.");
  elements.admissionsMobile.innerHTML = mobileCards(patient.admissions, [
    {label:"Entrada", key:"entry"}, {label:"Alta", key:"discharge"}, {label:"Quarto", key:"room"}, {label:"Observações", key:"notes"}
  ], "Nenhuma internação registrada.");
}
function showToast(message) {
  elements.toast.textContent = message; elements.toast.classList.add("show");
  window.clearTimeout(showToast.timer); showToast.timer = window.setTimeout(() => elements.toast.classList.remove("show"), 2200);
}
function closeMenu() {
  elements.sidebar.classList.remove("open"); elements.overlay.classList.remove("show");
  elements.menuButton.setAttribute("aria-expanded", "false");
}

elements.patientSelect.addEventListener("change", event => { renderPatient(event.target.value); elements.patientSearch.value = ""; });
elements.patientSearch.addEventListener("input", event => {
  const term = event.target.value.toLocaleLowerCase("pt-BR").replace(/\D/g, "");
  const raw = event.target.value.toLocaleLowerCase("pt-BR");
  const match = Object.entries(patients).find(([, p]) => p.name.toLocaleLowerCase("pt-BR").includes(raw) || (term && p.cpf.replace(/\D/g, "").includes(term)));
  if (match) { elements.patientSelect.value = match[0]; renderPatient(match[0]); }
});
elements.menuButton.addEventListener("click", () => {
  const open = elements.sidebar.classList.toggle("open"); elements.overlay.classList.toggle("show", open);
  elements.menuButton.setAttribute("aria-expanded", String(open));
});
elements.overlay.addEventListener("click", closeMenu);
document.addEventListener("keydown", event => { if (event.key === "Escape") closeMenu(); });
$("#printButton").addEventListener("click", () => window.print());
$("#globalSearch").addEventListener("keydown", event => { if (event.key === "Enter") showToast(`Busca por “${event.target.value}” enviada.`); });
$(".logout").addEventListener("click", () => showToast("Ação de saída pronta para integração com o backend."));

renderPatient("joao");
