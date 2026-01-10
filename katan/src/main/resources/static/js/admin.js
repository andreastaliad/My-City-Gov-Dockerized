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

        const csrfToken = form.querySelector("input[name='_csrf']").value;

        fetch("/admin/employees", {
            method: "POST",
            body: formData,
            headers: {
                "X-CSRF-TOKEN": csrfToken,
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(res => {
                if (!res.ok) throw new Error("Αποτυχία δημιουργίας υπαλλήλου");
                return res.text();
            })
            .then(html => {
                document.getElementById("adminTab").innerHTML = html;
            })
            .catch(err => alert(err.message));
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
            // ΜΕΝΕΙΣ ΣΤΟ ΙΔΙΟ TAB
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
            return loadContent(reloadUrl, 'adminTab');
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

