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

async function reloadEmployeeRequestsTab() {
    // Αυτό πρέπει να δείχνει στο div/container του tab που βάζεις το fragment content.
    // Π.χ. <div id="employee-requests-tab-content"></div>
    const container = document.querySelector('#employee-requests-tab-content');
    if (!container) return;

    const resp = await fetch('/employee/requests', {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    });

    if (!resp.ok) {
        // fallback: full reload (αν θες)
        window.location.reload();
        return;
    }

    container.innerHTML = await resp.text();
    wireEmployeeRequestActions(); // ξαναδένουμε handlers μετά το replace
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

            // Αν κάτι πάει στραβά (500), μην σου ανοίξει whitelabel. Κάνε reload το tab ή full reload.
            if (!resp.ok) {
                await reloadEmployeeRequestsTab();
                return;
            }

            // Ο controller σου επιστρέφει fragment HTML -> αντικαθιστούμε απευθείας το tab content
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

// αρχικό bind
document.addEventListener('DOMContentLoaded', wireEmployeeRequestActions);

