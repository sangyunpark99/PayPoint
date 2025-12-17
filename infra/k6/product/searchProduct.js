import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
    vus: 100,
    duration: "30s",
    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<1500"],
    },
};

const BASE_URL = "http://localhost:8080/api";
const ENDPOINT = "/product";
const SIZE = 20;

const SEED_BUILD_PAGES = 300;

const RESET_PROB = 0.15;

const CHAIN_MIN = 1;
const CHAIN_MAX = 5;

function buildUrl(cursorCreatedAt, cursorId) {
    const params = [`size=${SIZE}`];
    if (cursorCreatedAt) params.push(`cursorCreatedAt=${encodeURIComponent(cursorCreatedAt)}`);
    if (cursorId != null) params.push(`cursorId=${cursorId}`);
    return `${BASE_URL}${ENDPOINT}?${params.join("&")}`;
}


function pickSeed(seeds) {
    const r = Math.random();
    const n = seeds.length;
    if (n === 1) return seeds[0];

    if (r < 0.7) {
        const upTo = Math.min(n, Math.max(2, Math.floor(n * 0.2)));
        return seeds[Math.floor(Math.random() * upTo)];
    } else if (r < 0.95) {
        const start = Math.floor(n * 0.3);
        const end = Math.floor(n * 0.7);
        const idx = start + Math.floor(Math.random() * Math.max(1, end - start + 1));
        return seeds[idx];
    } else {
        const start = Math.floor(n * 0.85);
        const idx = start + Math.floor(Math.random() * Math.max(1, n - start));
        return seeds[idx];
    }
}

export function setup() {
    let cursorCreatedAt = null;
    let cursorId = null;

    const seeds = [];
    for (let page = 0; page < SEED_BUILD_PAGES; page++) {
        const url = buildUrl(cursorCreatedAt, cursorId);
        const res = http.get(url, { headers: { Accept: "application/json" } });

        if (res.status !== 200) {
            throw new Error(`setup failed: status=${res.status}, url=${url}, body=${res.body}`);
        }

        const body = res.json();

        if (body.nextCursorCreatedAt && body.nextCursorId != null) {
            seeds.push({
                cursorCreatedAt: body.nextCursorCreatedAt,
                cursorId: body.nextCursorId,
            });
        }

        if (!body.hasNext) break;

        cursorCreatedAt = body.nextCursorCreatedAt;
        cursorId = body.nextCursorId;
        if (!cursorCreatedAt || cursorId == null) break;
    }

    if (seeds.length === 0) {
        throw new Error("setup failed: no seeds created. Check endpoint/response fields.");
    }

    return { seeds };
}

let curCreatedAt = null;
let curId = null;

export default function (data) {
    const seeds = data.seeds;

    if (curCreatedAt == null || curId == null || Math.random() < RESET_PROB) {
        const seed = pickSeed(seeds);
        curCreatedAt = seed.cursorCreatedAt;
        curId = seed.cursorId;
    }

    const chain = CHAIN_MIN + Math.floor(Math.random() * (CHAIN_MAX - CHAIN_MIN + 1));

    for (let i = 0; i < chain; i++) {
        const url = buildUrl(curCreatedAt, curId);

        const res = http.get(url, {
            headers: { Accept: "application/json" },
            tags: { name: "GET /product (cursor)" },
        });

        const ok = check(res, {
            "status 200": (r) => r.status === 200,
            "has body": (r) => r.body && r.body.length > 0,
        });

        if (!ok) break;

        const body = res.json();

        if (!body.hasNext || !body.nextCursorCreatedAt || body.nextCursorId == null) {
            curCreatedAt = null;
            curId = null;
            break;
        }

        curCreatedAt = body.nextCursorCreatedAt;
        curId = body.nextCursorId;
    }

    sleep(0.05);
}