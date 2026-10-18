let routeMode = null;

let map = null;

let start = null;
let end = null;
let startMarker = null;
let endMarker = null;
let selectingStart = true;

async function startTest() {
    showScreen("route-screen");

    if (map === null) {
        await initMap();
    }
}

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
                            map,
                            start,
                            "S",
                            "start-marker"
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
                            map,
                            end,
                            "E",
                            "end-marker"
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

async function generate() {
    if (startEqualsEndCheckbox.checked) {
        end = start;
    }

    if (start === null || end === null) {
        alert("Выберите Start и End");
        return;
    }

    switch (routeMode) {
        case "ab":
            await generateAb();
            break;

        case "eval":
            await generateEval();
            break;

        default:
            throw new Error(
                `Unknown route mode: ${routeMode}`
            );
    }
}




function createMarker(targetMap, coordinates, text, className = "") {
    const element = document.createElement("div");

    element.className = `point-marker ${className}`;
    element.textContent = text;

    const marker = new ymaps3.YMapMarker(
        {
            coordinates
        },
        element
    );

    targetMap.addChild(marker);

    return marker;
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

function toYandexCoordinates(route) {
    return route.coordinates.map(c => [
        c.longitude,
        c.latitude
    ]);
}

function addRouteEndpoints(targetMap, route) {
    const coordinates = toYandexCoordinates(route);

    if (coordinates.length === 0) {
        return;
    }

    const start = coordinates[0];
    const end = coordinates[coordinates.length - 1];

    if (sameCoordinates(start, end)) {
        createMarker(
            targetMap,
            start,
            "S/E",
            "start-end-marker"
        );

        return;
    }

    createMarker(
        targetMap,
        start,
        "S",
        "start-marker"
    );

    createMarker(
        targetMap,
        end,
        "E",
        "end-marker"
    );
}

function sameCoordinates(a, b) {
    const eps = 1e-6;

    return Math.abs(a[0] - b[0]) < eps &&
        Math.abs(a[1] - b[1]) < eps;
}