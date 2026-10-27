let evalResult = null;
let evalIndex = 0;

let evaluationMap = null;
let routeFeature = null;

let selectedRating = null;
let selectedReasons = new Set();

async function startEvaluation() {
    startTest();

    routeMode = "eval";

    evalResult = null;
    evalIndex = 0;

    if (evaluationMap !== null) {
        evaluationMap.destroy();
        evaluationMap = null;
    }

    routeFeature = null;
}

async function generateEval() {
    const request = {
        username: state.username,

        start: {
            longitude: start[0],
            latitude: start[1]
        },

        end: {
            longitude: end[0],
            latitude: end[1]
        },

        durationMinutes:
            Number(document.getElementById("duration").value),

        count:
            Number(document.getElementById("count").value)
    };

    console.log("generate request:", request);

    const response = await fetch(
        "/api/evaluation/generate",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(request)
        }
    );

    if (!response.ok) {
        throw new Error(`Generate failed: ${response.status}`);
    }

    evalResult = await response.json();
    console.log("result:", evalResult);

    evalIndex = 0;

    document.getElementById("eval-content").hidden = false;

    renderCurrentEvaluation();
}

function renderCurrentEvaluation() {
    if (evalIndex >= evalResult.evaluations.length) {
        showEvalFinished();
        return;
    }

    resetEvalAnswer();

    const evaluation = evalResult.evaluations[evalIndex];

    document.getElementById("eval-progress")
        .textContent = `Evaluation ${evalIndex + 1} / ${evalResult.evaluations.length}`;

    document.getElementById("eval-route-info").textContent =
        formatRouteInfo(evaluation.route);

    createEvaluationMap(
        evaluation.route
    );

    renderRating(evalResult.maxRating);

    renderReasons(evalResult.reasons);
}

function createEvaluationMap(route) {
    const {
        YMap,
        YMapDefaultSchemeLayer,
        YMapDefaultFeaturesLayer,
        YMapFeature
    } = ymaps3;

    if (evaluationMap !== null) {
        evaluationMap.destroy();
    }

    const bounds = calculateBounds(route, route);

    console.log("bounds:", bounds);

    evaluationMap = new YMap(
        document.getElementById("eval-map"),
        {
            location: {
                bounds
            }
        }
    );

    evaluationMap.addChild(new YMapDefaultSchemeLayer());

    evaluationMap.addChild(new YMapDefaultFeaturesLayer());

    routeFeature = new YMapFeature({
        geometry: {
            type: "LineString",
            coordinates: toYandexCoordinates(route)
        },
        style: {
            stroke: [{
                color: "#2563eb",
                width: 6
            }]
        }
    });

    addRouteEndpoints(
        evaluationMap,
        route
    );

    evaluationMap.addChild(routeFeature);
}

function renderRating(maxRating) {
    const container = document.getElementById("eval-rating");

    container.innerHTML = "";

    for (let rating = 1; rating <= maxRating; rating++) {
        const button = document.createElement("button");

        button.textContent = rating;
        button.dataset.rating = rating;

        button.addEventListener("click", () => {
            selectedRating = rating;
            renderSelectedRating();
        });

        container.appendChild(button);
    }
}

function renderSelectedRating() {
    document
        .querySelectorAll("#eval-rating button")
        .forEach(button => {
            button.classList.toggle(
                "selected",
                Number(button.dataset.rating) === selectedRating
            );
        });
}

function renderReasons(reasons) {
    const container = document.getElementById("eval-reasons");

    container.innerHTML = "";

    for (const reason of reasons) {
        const label = document.createElement("label");

        const checkbox = document.createElement("input");

        checkbox.type = "checkbox";
        checkbox.value = reason.value;

        checkbox.addEventListener("change", () => {
            if (checkbox.checked) {
                selectedReasons.add(reason.value);
            } else {
                selectedReasons.delete(reason.value);
            }
        });

        label.appendChild(checkbox);
        label.append(reason.text);

        container.appendChild(label);
    }
}

const otherText = document.getElementById("eval-other-text");

function resetEvalAnswer() {
    selectedRating = null;
    selectedReasons.clear();
    otherText.value = "";
}


document
    .getElementById("eval-submit")
    .addEventListener("click", submitEval);

async function submitEval() {
    if (selectedRating === null) {
        return;
    }

    const evaluation = evalResult.evaluations[evalIndex];

    const other = otherText.value.trim();

    const response = await fetch(
        "/api/evaluation/evaluate",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                evaluationId: evaluation.id,
                rating: selectedRating,
                reasons: [...selectedReasons],
                other: other === "" ? null : other
            })
        }
    );

    if (!response.ok) {
        throw new Error(
            `Eval vote failed: ${response.status}`
        );
    }

    evalIndex++;
    renderCurrentEvaluation();
}


function showEvalFinished() {
    evaluationMap?.destroy();
    evaluationMap = null;

    document.getElementById("eval-content").hidden = true;
    document.getElementById("test-finished").hidden = false;
}