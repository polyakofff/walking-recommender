let abResult = null;
let abIndex = 0;

let comparisonMap = null;
let routeAFeature = null;
let routeBFeature = null;

async function startAb() {
    startTest();

    routeMode = "ab";

    abResult = null;
    abIndex = 0;

    if (comparisonMap !== null) {
        comparisonMap.destroy();
        comparisonMap = null;
    }

    routeAFeature = null;
    routeBFeature = null;
}

async function generateAb() {
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
        "/api/ab/generate",
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

    abResult = await response.json();
    console.log("result:", abResult);

    abIndex = 0;

    document.getElementById("ab-content").hidden = false;

    renderCurrentComparison();
}

function renderCurrentComparison() {
    if (abIndex >= abResult.comparisons.length) {
        showAbFinished();
        return;
    }

    const comparison = abResult.comparisons[abIndex];

    document.getElementById("ab-progress")
        .textContent = `Comparison ${abIndex + 1} / ${abResult.comparisons.length}`;

    document.getElementById("route-a-info").textContent =
        formatRouteInfo(comparison.routeA);

    document.getElementById("route-b-info").textContent =
        formatRouteInfo(comparison.routeB);

    createComparisonMap(
        comparison.routeA,
        comparison.routeB
    );
}

function createComparisonMap(routeA, routeB) {
    const {
        YMap,
        YMapDefaultSchemeLayer,
        YMapDefaultFeaturesLayer,
        YMapFeature
    } = ymaps3;

    if (comparisonMap !== null) {
        comparisonMap.destroy();
    }

    const bounds = calculateBounds(routeA, routeB);

    console.log("bounds:", bounds);

    comparisonMap = new YMap(
        document.getElementById("ab-map"),
        {
            location: {
                bounds
            }
        }
    );

    comparisonMap.addChild(new YMapDefaultSchemeLayer());

    comparisonMap.addChild(new YMapDefaultFeaturesLayer());

    routeAFeature = new YMapFeature({
        geometry: {
            type: "LineString",
            coordinates: toYandexCoordinates(routeA)
        },
        style: {
            stroke: [{
                color: "#2563eb",
                width: 6
            }]
        }
    });

    routeBFeature = new YMapFeature({
        geometry: {
            type: "LineString",
            coordinates: toYandexCoordinates(routeB)
        },
        style: {
            stroke: [{
                color: "#f97316",
                width: 6
            }]
        }
    });

    comparisonMap.addChild(routeAFeature);

    comparisonMap.addChild(routeBFeature);

    addRouteEndpoints(
        comparisonMap,
        routeA // routeA.start/end = routeB.start/end
    );

    document.getElementById("show-a").checked = true;
    document.getElementById("show-b").checked = true;
}

document
    .getElementById("show-a")
    .addEventListener("change", function () {
        if (this.checked) {
            if (document.getElementById("show-b").checked) {
                comparisonMap.removeChild(routeBFeature);
            }

            comparisonMap.addChild(routeAFeature);

            if (document.getElementById("show-b").checked) {
                comparisonMap.addChild(routeBFeature);
            }
        } else {
            comparisonMap.removeChild(routeAFeature);
        }
    });

document
    .getElementById("show-b")
    .addEventListener("change", function () {
        if (this.checked) {
            if (document.getElementById("show-a").checked) {
                comparisonMap.removeChild(routeAFeature);
            }

            comparisonMap.addChild(routeBFeature);

            if (document.getElementById("show-a").checked) {
                comparisonMap.addChild(routeAFeature);
            }
        } else {
            comparisonMap.removeChild(routeBFeature);
        }
    });


document
    .querySelectorAll("[data-choice]")
    .forEach(button => {
        button.addEventListener(
            "click",
            () => vote(button.dataset.choice)
        );
    });

async function vote(choice) {
    const comparison = abResult.comparisons[abIndex];

    const response = await fetch(
        `/api/ab/vote`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                comparisonId: comparison.id,
                choice: choice
            })
        }
    );

    if (!response.ok) {
        throw new Error(`Vote failed: ${response.status}`);
    }

    abIndex++;
    renderCurrentComparison();
}

function showAbFinished() {
    comparisonMap?.destroy();
    comparisonMap = null;

    document.getElementById("ab-content").hidden = true;
    document.getElementById("test-finished").hidden = false;
}

