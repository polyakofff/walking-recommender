let setup = null;
let setupQuestionIndex = 0;
let setupAnswers = {};
let selectedAnswer = null;

async function startSetup() {
    showScreen("setup-screen");

    setup = await fetch("/api/setup")
        .then(response => response.json());

    console.log("setup: ", setup);

    setupQuestionIndex = 0;
    setupAnswers = {};

    showSetupQuestion();
}

function showSetupQuestion() {
    const question = setup.questions[setupQuestionIndex];

    document.getElementById("setup-progress")
        .textContent = `${setupQuestionIndex + 1} / ${setup.questions.length}`;

    document.getElementById("setup-question")
        .textContent = question.text;

    const answersContainer =
        document.getElementById("setup-answers");

    answersContainer.innerHTML = "";

    selectedAnswer = setupAnswers[question.feature] ?? null;

    for (const answer of setup.answers) {
        const button = document.createElement("button");

        button.textContent = answer.text;

        button.onclick = () => {
            selectedAnswer = answer.value;

            setupAnswers[question.feature] = answer.value;

            renderSelectedAnswer();
        };

        button.dataset.value = answer.value;

        answersContainer.appendChild(button);
    }

    renderSelectedAnswer();

    const nextButton =
        document.getElementById("setup-next");

    nextButton.textContent = setupQuestionIndex === setup.questions.length - 1
        ? "Завершить"
        : "Далее";
}

function renderSelectedAnswer() {
    document
        .querySelectorAll("#setup-answers button")
        .forEach(button => {
            button.classList.toggle(
                "selected",
                button.dataset.value === selectedAnswer
            );
        });

    document.getElementById("setup-next")
        .disabled = selectedAnswer === null;
}


document
    .getElementById("setup-next")
    .addEventListener("click", async () => {

        if (selectedAnswer === null) {
            return;
        }

        if (setupQuestionIndex < setup.questions.length - 1) {
            setupQuestionIndex++;
            showSetupQuestion();
            return;
        }

        await finishSetup();
    });

async function finishSetup() {
    const username = state.username;

    const response = await fetch(
        `/api/user/${encodeURIComponent(username)}/preferences`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                answers: setupAnswers
            })
        }
    );

    if (!response.ok) {
        throw new Error("Failed to save preferences");
    }

    state.user = await response.json();

    showMainScreen();
}