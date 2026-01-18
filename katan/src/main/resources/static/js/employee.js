//Employee UI JavaScript

//Φόρτωση fragment
function loadContent(url, targetId) {
    const target = document.getElementById(targetId);
    if (!target) return;

    fetch(url, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(r => {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.text();
        })
        .then(html => {
            target.innerHTML = html;

            wireEmployeeRequestActions();
            wireEmployeeRescheduleDropdowns();
        })
        .catch(err => {
            console.error(err);
            target.innerHTML = `<div class="alert alert-danger">Σφάλμα φόρτωσης περιεχομένου.</div>`;
        });
}

//Υποβολή φόρμας με AJAX και δυναμική ανανέωση fragment
async function postFormAndReloadEmployeeRequests(form) {
    const body = new URLSearchParams(new FormData(form));

    const response = await fetch(form.action, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: body
    });

    const html = await response.text();

    // Το fragment ΠΡΕΠΕΙ να αντικαθιστά αυτό το container
    const container = document.querySelector("#employeeRequestsContent");
    if (container) {
        container.outerHTML = html;
    } else {
        // fallback – αν κάτι πάει στραβά
        window.location.reload();
    }
}

//Ανανέωση tab με αποφυγή page reload
async function reloadEmployeeRequestsTab() {
    const container = document.querySelector('#employee-requests-tab-content');
    if (!container) return;

    const resp = await fetch('/employee/requests', {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    });

    if (!resp.ok) {
        // fallback: full reload
        window.location.reload();
        return;
    }

    container.innerHTML = await resp.text();
    wireEmployeeRequestActions(); //ξαναδένουμε handlers μετά το replace
}

function wireEmployeeRequestActions() {
    document.querySelectorAll('form.js-req-action').forEach(form => {
        if (form.dataset.bound === '1') return;
        form.dataset.bound = '1';

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const resp = await fetch(form.action, {
                method: form.method || 'POST',
                body: new FormData(form),
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });

            // Αν κάτι πάει στραβά (500), να μην ανοίξει whitelabel. Κάνει reload το tab ή full reload.
            if (!resp.ok) {
                await reloadEmployeeRequestsTab();
                return;
            }

            // Ο controller επιστρέφει fragment HTML -> αντικαθιστούμε απευθείας το tab content
            const html = await resp.text();
            const container = document.querySelector('#employee-requests-tab-content');
            if (container) {
                container.innerHTML = html;
                wireEmployeeRequestActions();
            } else {
                // fallback
                await reloadEmployeeRequestsTab();
            }
        });
    });
}

function postFormAndReloadEmployeeAppointments(form) {
    fetch(form.action, {
        method: 'POST',
        body: new FormData(form),
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(r => {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.text();
        })
        .then(html => {
            const target = document.getElementById('employeeAppointmentsContent');
            if (!target) {
                console.error('employeeAppointmentsContent not found in DOM');
                return;
            }
            target.innerHTML = html;
            //Μετά την αντικατάσταση, ξαναδένει τα dropdowns
            wireEmployeeRescheduleDropdowns();
        })
        .catch(err => {
            console.error(err);
            alert('Σφάλμα κατά τη διαχείριση ραντεβού');
        });
}

async function fetchJson(url) {
    const res = await fetch(url, { headers: { "Accept": "application/json" } });
    if (!res.ok) return [];
    return await res.json();
}

function fillSelect(select, placeholder, values, selectedValue) {
    select.innerHTML = "";

    const ph = document.createElement("option");
    ph.value = "";
    ph.disabled = true;
    ph.selected = !selectedValue;
    ph.textContent = placeholder;
    select.appendChild(ph);

    for (const v of values) {
        const opt = document.createElement("option");
        opt.value = v;
        opt.textContent = v;
        if (selectedValue && v === selectedValue) opt.selected = true;
        select.appendChild(opt);
    }

    select.disabled = false;
}

function setLoading(select, text) {
    select.innerHTML = "";
    const opt = document.createElement("option");
    opt.value = "";
    opt.disabled = true;
    opt.selected = true;
    opt.textContent = text;
    select.appendChild(opt);
    select.disabled = true;
}

//Φορτώνει τις ημερομηνίες και τις ώρες για κάθε γραμμή υπαλλήλου χωρίς reload
async function wireEmployeeRescheduleDropdowns() {
    const dateSelects = document.querySelectorAll("select.js-emp-date");
    if (!dateSelects.length) return;

    //Cache ανά serviceUnitId για να μην γίνουν 100 fetch
    const datesCache = new Map(); // suId -> ημερομηνίες
    const timesCache = new Map(); // suId|ημερομηνίες -> ώρες

    //Αντιστοίχιση ώρας με id ραντεβού
    const timeSelects = document.querySelectorAll("select.js-emp-time");
    const timeByApptId = new Map();
    timeSelects.forEach(ts => timeByApptId.set(ts.dataset.appointmentId, ts));

    for (const ds of dateSelects) {
        const apptId = ds.dataset.appointmentId;
        const suId = ds.dataset.serviceUnitId;
        const currentDate = ds.dataset.currentDate || ds.value || "";
        const ts = timeByApptId.get(String(apptId));
        const currentTime = ts?.dataset.currentTime || ts?.value || "";

        if (!suId) continue;

        //Ημερομηνίες
        if (!datesCache.has(suId)) {
            setLoading(ds, "Φόρτωση ημερομηνιών...");
            //Χρησιμοποιεί τα υπάρχοντα endpoints του πολίτη
            const dates = await fetchJson(`/appointments/available-dates?serviceUnitId=${encodeURIComponent(suId)}`);
            datesCache.set(suId, Array.isArray(dates) ? dates : []);
        }

        let dates = datesCache.get(suId) || [];
        //Κρατά την τρέχουσα ημερομηνία μέσα ασχέτως διαθεσιμότητας
        if (currentDate && !dates.includes(currentDate)) {
            dates = [currentDate, ...dates];
        }

        if (!dates.length) {
            fillSelect(ds, "-- Δεν υπάρχουν ημερομηνίες --", [], "");
            if (ts) fillSelect(ts, "-- Δεν υπάρχουν ώρες --", [], "");
            continue;
        }

        fillSelect(ds, "-- Επίλεξε ημερομηνία --", dates, currentDate);

        //Για την φόρτωση ωρών
        const loadTimes = async (date, preselectTime) => {
            if (!ts) return;
            if (!date) {
                fillSelect(ts, "-- Επίλεξε πρώτα ημερομηνία --", [], "");
                return;
            }

            const key = `${suId}|${date}`;
            if (!timesCache.has(key)) {
                setLoading(ts, "Φόρτωση ωρών...");
                const times = await fetchJson(
                    `/appointments/available-times?serviceUnitId=${encodeURIComponent(suId)}&date=${encodeURIComponent(date)}`
                );
                timesCache.set(key, Array.isArray(times) ? times : []);
            }

            let times = timesCache.get(key) || [];
            //Κρατά την τρέχουσα ώρα μέσα ασχέτως διαθεσιμότητας
            if (preselectTime && !times.includes(preselectTime)) {
                times = [preselectTime, ...times];
            }

            if (!times.length) {
                fillSelect(ts, "-- Δεν υπάρχουν ώρες --", [], "");
                return;
            }

            fillSelect(ts, "-- Επίλεξε ώρα --", times, preselectTime || "");
        };

        //Αρχικές τιμές για ημέρες/ώρες
        await loadTimes(currentDate, currentTime);

        //Όταν αλλάζει ημερομηνία φορτώνει τις διαθέσιμες ώρες(χωρίς πλήρες page reload)
        ds.addEventListener("change", async () => {
            await loadTimes(ds.value, "");
        });
    }
}

window.postFormAndReloadEmployeeAppointments = postFormAndReloadEmployeeAppointments;
window.postFormAndReloadEmployeeRequests = postFormAndReloadEmployeeRequests;
window.loadContent = loadContent;

document.addEventListener('DOMContentLoaded', () => {
    wireEmployeeRequestActions();
    wireEmployeeRescheduleDropdowns();
});

document.addEventListener('change', function (e) {
    const sel = e.target;
    if (!sel.classList.contains('js-decision')) return;

    const form = sel.closest('form');
    if (!form) return;

    const reason = form.querySelector('.js-reason');
    if (!reason) return;

    const decision = sel.value;
    reason.required = (decision === 'REJECTED');
}, true);

document.addEventListener('submit', function (e) {
    const form = e.target;
    if (!form.classList.contains('js-decision-form')) return;

    const decision = form.querySelector('.js-decision')?.value;
    const reason = (form.querySelector('.js-reason')?.value || '').trim();

    if (decision === 'REJECTED' && reason.length === 0) {
        e.preventDefault();
        alert('Η απόρριψη απαιτεί τεκμηρίωση.');
    }
}, true);
