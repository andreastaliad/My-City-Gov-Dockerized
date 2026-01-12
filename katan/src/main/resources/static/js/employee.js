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
