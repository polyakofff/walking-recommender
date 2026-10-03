alert('Спасибо!!');

initMap().catch(console.error);

let map = null;

let start = null;
let end = null;

let startMarker = null;
let endMarker = null;

let selectingStart = true;

const startEqualsEndCheckbox =
    document.getElementById("start-equals-end");

async function initMap() {
    // Промис `ymaps3.ready` будет зарезолвлен, когда загрузятся все компоненты основного модуля API
    await ymaps3.ready;

    const {
        YMap,
        YMapDefaultSchemeLayer,
        YMapDefaultFeaturesLayer,
        YMapListener,
    } = ymaps3;

    // Иницилиазируем карту
    map = new YMap(
        // Передаём ссылку на HTMLElement контейнера
        document.getElementById('map'),

        // Передаём параметры инициализации карты
        {
            location: {
                // Координаты центра карты
                center: [37.588144, 55.733842],

                // Уровень масштабирования
                zoom: 10
            }
        }
    );

    // Добавляем слой для отображения схематической карты
    map.addChild(new YMapDefaultSchemeLayer());

    map.addChild(new YMapDefaultFeaturesLayer());

    map.addChild(
        new YMapListener({
            onClick: (_, event) => {
                console.log("clicked:", event.coordinates);

                if (selectingStart) {
                    start = event.coordinates;

                    if (startMarker == null) {
                        startMarker = createMarker(
                            start,
                            "S"
                        );
                    } else {
                        startMarker.update({
                            coordinates: start
                        });
                    }

                    selectingStart = startEqualsEndCheckbox.checked;

                    document.getElementById("start-value")
                        .textContent = formatCoordinates(start);
                } else {
                    end = event.coordinates;

                    if (endMarker == null) {
                        endMarker = createMarker(
                            end,
                            "E"
                        );
                    } else {
                        endMarker.update({
                            coordinates: end
                        });
                    }

                    selectingStart = true;

                    document.getElementById("end-value")
                        .textContent = formatCoordinates(end);
                }
            }
        })
    );
}


startEqualsEndCheckbox.addEventListener("change", function () {
    if (startEqualsEndCheckbox.checked) {
        if (start !== null) {
            end = [...start];

            document.getElementById("end-value").textContent =
                "= Start";
        } else {
            end = null;

            document.getElementById("end-value").textContent =
                "не выбран";
        }

        // Если раньше уже был отдельный End-marker — убираем его
        if (endMarker !== null) {
            map.removeChild(endMarker);
            endMarker = null;
        }

        selectingStart = true;
    } else {
        // Теперь End снова надо выбрать отдельно
        end = null;

        document.getElementById("end-value").textContent =
            "не выбран";

        selectingStart = start == null;
    }
});


document
    .getElementById("generate")
    .addEventListener("click", generate);

let result = null;

let currentComparisonIndex = 0;

async function generate() {
    if (startEqualsEndCheckbox.checked) {
        end = start;
    }

    if (start === null || end === null) {
        alert("Выберите Start и End");
        return;
    }

    const durationMinutes = Number(document.getElementById("duration").value);

    const comparisonCount = Number(document.getElementById("count").value);

    const request = {
        start: {
            longitude: start[0],
            latitude: start[1]
        },
        end: {
            longitude: end[0],
            latitude: end[1]
        },
        durationMinutes,
        comparisonCount
    };

    console.log("generate request:", request);

    const response = await fetch("/api/ab/generate", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(request)
    });

    if (!response.ok) {
        throw new Error(
            `Generate failed: ${response.status}`
        );
    }

    result = await response.json();
    console.log("result:", result);

    currentComparisonIndex = 0;

    document.getElementById("comparison").hidden = false;

    renderCurrentComparison();
}

function renderCurrentComparison() {
    if (currentComparisonIndex >= result.comparisons.length) {
        // showFinished();
        return;
    }

    const comparison = result.comparisons[currentComparisonIndex];

    document.getElementById("comparison-progress")
        .textContent = `Comparison ${currentComparisonIndex + 1} / ${result.comparisons.length}`;

    document.getElementById("route-a-info").textContent =
        formatRouteInfo(comparison.routeA);

    document.getElementById("route-b-info").textContent =
        formatRouteInfo(comparison.routeB);

    createComparisonMap(
        comparison.routeA,
        comparison.routeB
    );
}

let comparisonMap = null;

let routeAFeature = null;
let routeBFeature = null;

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

    console.log("routeA:", routeA);
    console.log("routeB:", routeB);

    const bounds = calculateBounds(routeA, routeB);

    console.log("bounds:", bounds);

    comparisonMap = new YMap(
        document.getElementById("comparison-map"),
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
    const comparison =
        result.comparisons[currentComparisonIndex];

    await fetch(
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

    currentComparisonIndex++;

    renderCurrentComparison();
}




function createMarker(coordinates, text) {
    const element = document.createElement("div");

    element.className = "point-marker";
    element.textContent = text;

    const marker = new ymaps3.YMapMarker(
        {
            coordinates
        },
        element
    );

    map.addChild(marker);

    return marker;
}

function calculateBounds(routeA, routeB) {
    const coordinates = [
        ...routeA.coordinates,
        ...routeB.coordinates
    ];

    const longitudes =
        coordinates.map(it => it.longitude);

    const latitudes =
        coordinates.map(it => it.latitude);

    return [
        [
            Math.min(...longitudes),
            Math.min(...latitudes)
        ],
        [
            Math.max(...longitudes),
            Math.max(...latitudes)
        ]
    ];
}

function formatCoordinates(coordinates) {
    const [longitude, latitude] = coordinates;

    return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
}

function formatRouteInfo(route) {
    const km = (route.distance / 1000).toFixed(1);
    const minutes = Math.round(route.duration / 60);

    return `${km} km · ${minutes} min`;
}

function toYandexCoordinates(route) {
    return route.coordinates.map(c => [
        c.longitude,
        c.latitude
    ]);
}
