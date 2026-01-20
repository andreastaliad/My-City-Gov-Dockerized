//Admin UI JavaScript

function csrfHeaders() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = {};
    if (tokenMeta && headerMeta) headers[headerMeta.content] = tokenMeta.content;
    return headers;
}

function submitRequestType(form) {
    fetch(form.action, {
        method: 'POST',
        body: new FormData(form),
        credentials: 'same-origin',
        headers: csrfHeaders()
    })
        .then(async res => {
            if (!res.ok) throw new Error(await res.text());
            return res.text();
        })
        .then(html => {
            document.getElementById("adminTab").innerHTML = html;
            alert("Ο τύπος αιτήματος δημιουργήθηκε επιτυχώς");
        })
        .catch(err => {
            console.error(err);
            alert("Σφάλμα αποθήκευσης τύπου αιτήματος");
        });
}

function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

function submitServiceUnit(form) {
    fetch(form.action, {
        method: 'POST',
        body: new FormData(form),
        credentials: 'same-origin',
        headers: csrfHeaders()
    }).then(res => {
        if (!res.ok) {
            alert("Αποτυχία αποθήκευσης υπηρεσίας");
            return;
        }
        alert("Η υπηρεσία αποθηκεύτηκε επιτυχώς");
        loadContent('/admin/service-units', 'adminTab');
    });
}

function submitToggle(form) {
    fetch(form.action, {
        method: 'POST',
        credentials: 'same-origin',
        headers: csrfHeaders()
    }).then(res => {
        if (!res.ok) {
            alert("Αποτυχία εναλλαγής τύπου αιτήματος");
            return;
        }
        alert("Η εναλλαγή τύπου αιτήματος ολοκληρώθηκε επιτυχώς");
        loadContent('/admin/request-types', 'adminTab');
    });
}

//συνάρτηση για τον admin σχετικά με την επεξεργασία προβλημάτων
function submitIssueStatus(form) {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');

    const headers = {};
    if (tokenMeta && headerMeta) {
        headers[headerMeta.content] = tokenMeta.content;
    }

    fetch(form.action, {
        method: 'POST',
        credentials: 'same-origin',
        headers: headers,
        body: new FormData(form)
    }).then(response => {
        if (!response.ok) {
            document.getElementById("issueAlert").innerHTML = `
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    Αποτυχία αποθήκευσης κατάστασης
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            `;
            return;
        }

        document.getElementById("issueAlert").innerHTML = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                 Η κατάσταση του προβλήματος ενημερώθηκε επιτυχώς
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        // Μικρή καθυστέρηση για να προλάβει να φανεί το μήνυμα
        setTimeout(() => {
            loadContent('/admin/issues', 'adminTab');
        }, 800);
    })
        .catch(() => {
            document.getElementById("issueAlert").innerHTML = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                Σφάλμα επικοινωνίας με τον server
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        });
}

//συνάρτηση για τη φόρμα υπαλλήλων
function attachEmployeeFormHandler() {
    const form = document.getElementById("employeeForm");
    if (!form) return;

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const formData = new FormData(form);
        const headers = {
            "X-Requested-With": "XMLHttpRequest",
            ...csrfHeaders()
        };

        fetch(form.action || "/admin/employees", {
            method: "POST",
            body: formData,
            credentials: 'same-origin',
            headers: headers
        })
            .then(() => {
                alert("Οι υπάλληλοι αποθηκεύτηκαν επιτυχώς");
                loadContent('/admin/users/employees', 'adminTab');
            })
            .catch(() => {
                alert("Αποτυχία αποθήκευσης υπαλλήλων");
                loadContent('/admin/users/employees', 'adminTab');
            });
    });
}

function submitRequestTypeToggle(form) {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');

    const headers = {};
    if (tokenMeta && headerMeta) {
        headers[headerMeta.content] = tokenMeta.content;
    }

    fetch(form.action, {
        method: 'POST',
        credentials: 'same-origin',
        headers: headers
    })
        .then(res => {
            if (!res.ok) {
                alert("Αποτυχία εναλλαγής τύπου αιτήματος");
                return;
            }
            alert("Η εναλλαγή τύπου αιτήματος ολοκληρώθηκε επιτυχώς");
            loadContent('/admin/request-types', 'adminTab');
        })
        .catch(() => alert("Σφάλμα επικοινωνίας με τον server"));
}

function submitServiceUnitToggle(form) {
    fetch(form.action, {
        method: 'POST',
        credentials: 'same-origin',
        headers: csrfHeaders()
    })
        .then(res => {
            if (!res.ok) {
                alert("Αποτυχία αλλαγής κατάστασης υπηρεσίας");
                return;
            }
            alert("Η αλλαγή κατάστασης υπηρεσίας ολοκληρώθηκε επιτυχώς");
            //Μένει στο ιδιο tab
            loadContent('/admin/service-units', 'adminTab');
        })
        .catch(() => alert("Σφάλμα επικοινωνίας με τον server"));
}

function submitPostAndReload(form, reloadUrl) {
    fetch(form.action, {
        method: 'POST',
        body: new FormData(form),
        credentials: 'same-origin',
        headers: csrfHeaders()
    })
        .then(async res => {
            if (!res.ok) {
                const text = await res.text();
                console.error("POST failed:", res.status, text);
                throw new Error("POST failed");
            }
            // reload the tab content after successful action
            loadContent(reloadUrl, 'adminTab');
            alert("Η ενέργεια ολοκληρώθηκε επιτυχώς");
        })
        .catch(() => alert("Αποτυχία ενέργειας"));
}

function submitScheduleAction(form) {
    const idEl = document.getElementById("serviceUnitId");
    if (!idEl) {
        console.error("Missing #serviceUnitId");
        alert("Αποτυχία ενέργειας");
        return;
    }
    const sid = idEl.value;
    submitPostAndReload(form, '/admin/service-units/' + sid + '/schedules');
}

function submitEmployeesAction(form) {
    if (!form) {
        console.error("submitEmployeesAction called without form");
        alert("Δεν βρέθηκε φόρμα. Δες το onsubmit.");
        return;
    }
    const sidEl = document.getElementById("serviceUnitEmployeesId");
    if (!sidEl) {
        console.error("Missing #serviceUnitEmployeesId");
        alert("Λείπει το serviceUnitEmployeesId");
        return;
    }
    const sid = sidEl.value;
    submitPostAndReload(form, '/admin/service-units/' + sid + '/employees');
}

function saveServiceUnitEmployees(serviceUnitId) {
    const select = document.getElementById('employees-' + serviceUnitId);
    if (!select) {
        console.error("SELECT NOT FOUND", serviceUnitId);
        return;
    }

    const body = Array.from(select.selectedOptions)
        .map(o => `employeeIds=${o.value}`)
        .join('&');

    fetch(`/admin/service-units/${serviceUnitId}/employees`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            ...csrfHeaders()   
        },
        body: body
    })
        .then(res => {
            if (!res.ok) {
                console.error("SAVE FAILED", res.status);
                alert("Αποτυχία ανάθεσης υπαλλήλων");
                return;
            }
            alert("Η ανάθεση υπαλλήλων αποθηκεύτηκε επιτυχώς");
            loadContent('/admin/service-units', 'adminTab');
        });
}

function postFormAndReloadAdminRequests(form) {
    const url = form.action;
    const formData = new FormData(form);

    fetch(url, {
        method: 'POST',
        body: formData,
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Request failed');
            }
            return response.text();
        })
        .then(html => {
            document.getElementById('adminTab').innerHTML = html;
            alert('Η ανάθεση αιτήματος ολοκληρώθηκε επιτυχώς');
        })
        .catch(err => {
            console.error(err);
            alert('Σφάλμα κατά την ανάθεση αιτήματος');
        });
}
