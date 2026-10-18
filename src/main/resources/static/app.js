const state = {
    username: null,
    user: null,
};


init();

async function init() {
    const username = localStorage.getItem("username");

    if (!username) {
        showScreen("login-screen");
        return;
    }

    const response = await fetch(
        `/api/user/${encodeURIComponent(username)}`
    );

    if (!response.ok) {
        localStorage.removeItem("username");
        showScreen("login-screen");
        return;
    }

    state.username = username;
    state.user = await response.json();

    showMainScreen();
}

function showScreen(screenId) {
    document
        .querySelectorAll(".screen")
        .forEach(screen => {
            screen.classList.add("hidden");
        });

    document
        .getElementById(screenId)
        .classList.remove("hidden");
}

document
    .getElementById("login-button")
    .addEventListener("click", login);

document
    .getElementById("username-input")
    .addEventListener("keydown", event => {
        if (event.key === "Enter") {
            login();
        }
    });

async function login() {
    const input = document.getElementById("username-input");

    const username = input.value.trim();

    if (!username) {
        return;
    }

    const response = await fetch(
        "/api/user",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                username: username,
            }),
        }
    );

    if (!response.ok) {
        throw new Error("Failed to login");
    }

    const user = await response.json();

    state.username = username;
    state.user = user;

    localStorage.setItem("username", username);

    showMainScreen();
}

function showMainScreen() {
    showScreen("main-screen");

    document
        .getElementById("welcome")
        .textContent = `Привет, ${state.username}`;

    updateMainScreen();
}

function updateMainScreen() {
    const configured = state.user?.configured === true;

    const abButton =
        document.getElementById("ab-button");

    const evalButton =
        document.getElementById("eval-button");

    const message =
        document.getElementById(
            "test-disabled-message"
        );

    abButton.disabled = !configured;
    evalButton.disabled = !configured;

    message.classList.toggle(
        "hidden",
        configured
    );
}


document
    .getElementById("setup-button")
    .addEventListener("click", () => {
        startSetup();
    });

document
    .getElementById("setup-back")
    .addEventListener("click", () => {
        showMainScreen();
    });

document
    .getElementById("ab-button")
    .addEventListener("click", () => {
        startAb();
    });

document
    .getElementById("eval-button")
    .addEventListener("click", () => {
        startEvaluation();
    });

document
    .getElementById("route-back")
    .addEventListener("click", () => {
        showMainScreen();
    });

document
    .getElementById("test-finished-back")
    .addEventListener("click", () => {
        showMainScreen();
    });



document
    .getElementById("change-user-button")
    .addEventListener("click", logout);

function logout() {
    localStorage.removeItem("username");

    state.username = null;
    state.user = null;

    document
        .getElementById("username-input")
        .value = "";

    showScreen("login-screen");
}