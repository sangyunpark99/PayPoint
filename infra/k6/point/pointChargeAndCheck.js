import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { baseSetup, baseTeardown } from '../common/time.js';
import crypto from 'k6/crypto';

const BASE_URL = 'http://localhost:8080/api/point';

const MAX_USER_ID = 3000;

export const chargeRequests = new Counter('charge_requests');
export const historyRequests = new Counter('history_requests');

export const options = {
    scenarios: {
        charge_scenario: {
            executor: 'constant-vus',
            vus: 100,
            duration: '1m',
            exec: 'charge',
        },
        history_scenario: {
            executor: 'constant-vus',
            vus: 200,             // 읽기 역할
            duration: '1m',
            exec: 'history',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'], // 전체 기준 예시
        'http_req_duration{type:charge}': ['p(95)<500'],
        'http_req_duration{type:history}': ['p(95)<300'],
    },
};

function randomUserId() {
    return Math.floor(Math.random() * MAX_USER_ID) + 1;
}

function randomPoint() {
    return Math.floor(Math.random() * 1000) + 1;
}

export function charge() {
    const userId = randomUserId();

    const url = 'http://localhost:8080/api/point/charge';

    const payload = JSON.stringify({
        userId: userId,
        point: randomPoint(),
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Idempotency-Key': idempotencyKey
        },
        tags: { type: 'charge' },
    };

    const res = http.post(`${BASE_URL}/charge`, payload, params);

    chargeRequests.add(1);

    check(res, {
        'charge status is 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    sleep(1);
}

export function history() {
    const userId = randomUserId();

    const date = '2025-12-01T00:00:00';

    const params = {
        tags: { type: 'history' },
    };

    const res = http.get(
        `${BASE_URL}/${userId}/detail?startDate=${date}`,
        params,
    );

    historyRequests.add(1);

    check(res, {
        'history status is 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    sleep(1);
}

export const setup = baseSetup;
export const teardown = baseTeardown;