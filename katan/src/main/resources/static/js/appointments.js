//Appointments Booking JavaScript

(function () {
    function resetSelect(select, msg) {
        select.innerHTML = "";
        const opt = document.createElement("option");
        opt.value = "";
        opt.disabled = true;
        opt.selected = true;
        opt.textContent = msg;
        select.appendChild(opt);
        select.disabled = true;
    }

    function fillSelect(select, placeholder, values) {
        select.innerHTML = "";
        const ph = document.createElement("option");
        ph.value = "";
        ph.disabled = true;
        ph.selected = true;
        ph.textContent = placeholder;
        select.appendChild(ph);

        for (const v of values) {
            const opt = document.createElement("option");
            opt.value = v;
            opt.textContent = v;
            select.appendChild(opt);
        }
        select.disabled = false;
    }

    async function fetchJson(url) {
        const res = await fetch(url, { headers: { "Accept": "application/json" } });
        if (!res.ok) return [];
        return await res.json();
    }

    function bindAppointmentsIfPresent() {
        const form = document.getElementById("appointmentForm");
        if (!form) return;

        // Αν έχει ήδη δεθεί, δεν ξανακάνει init
        if (form.dataset.bound === "1") return;
        form.dataset.bound = "1";

        const serviceEl = document.getElementById("serviceUnitId");
        const dateEl = document.getElementById("date"); // dropdown ημερομηνιών (select)
        const timeEl = document.getElementById("time"); // dropdown ωρών (select)

        if (!serviceEl || !dateEl || !timeEl) return;

        // αρχική κατάσταση ΜΟΝΟ μία φορά
        resetSelect(dateEl, "-- Επίλεξε πρώτα υπηρεσία --");
        resetSelect(timeEl, "-- Επίλεξε πρώτα ημερομηνία --");

        serviceEl.addEventListener("change", async () => {
            const serviceUnitId = serviceEl.value;

            resetSelect(dateEl, "-- Φόρτωση ημερομηνιών --");
            resetSelect(timeEl, "-- Επίλεξε πρώτα ημερομηνία --");

            if (!serviceUnitId) {
                resetSelect(dateEl, "-- Επίλεξε πρώτα υπηρεσία --");
                return;
            }

            const dates = await fetchJson(
                `/appointments/available-dates?serviceUnitId=${encodeURIComponent(serviceUnitId)}&daysAhead=21`
            );

            if (!dates.length) {
                resetSelect(dateEl, "-- Δεν υπάρχουν διαθέσιμες ημερομηνίες --");
                return;
            }

            fillSelect(dateEl, "-- Επίλεξε ημερομηνία --", dates);
        });

        dateEl.addEventListener("change", async () => {
            const serviceUnitId = serviceEl.value;
            const date = dateEl.value;

            resetSelect(timeEl, "-- Φόρτωση ωρών --");

            if (!serviceUnitId || !date) {
                resetSelect(timeEl, "-- Επίλεξε πρώτα ημερομηνία --");
                return;
            }

            const times = await fetchJson(
                `/appointments/available-times?serviceUnitId=${encodeURIComponent(serviceUnitId)}&date=${encodeURIComponent(date)}`
            );

            if (!times.length) {
                resetSelect(timeEl, "-- Δεν υπάρχουν διαθέσιμες ώρες --");
                return;
            }

            fillSelect(timeEl, "-- Επίλεξε ώρα --", times);
        });
    }

    // αρχικό load
    document.addEventListener("DOMContentLoaded", bindAppointmentsIfPresent);

    // fragments: παρακολούθηση DOM για να κάνουμε init όταν μπει το fragment
    const obs = new MutationObserver(() => bindAppointmentsIfPresent());
    obs.observe(document.documentElement, { childList: true, subtree: true });
})();
