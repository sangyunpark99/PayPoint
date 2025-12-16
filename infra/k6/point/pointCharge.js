import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomSeed } from 'k6';
import crypto from 'k6/crypto';

randomSeed(1234);

export const options = {
    scenarios: {
        constant_rate: {
            executor: 'constant-arrival-rate',
            rate: 3000,
            timeUnit: '1s',
            duration: '60s',
            preAllocatedVUs: 500,
            maxVUs:2000
        }
    }
};

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default function () {
    const userId = randomInt(1, 100);
    const point = randomInt(1, 100) * 10;
    const url = 'http://localhost:8080/api/point/charge';

    const idempotencyKey = crypto.randomUUID();

    const payload = JSON.stringify({
        userId: userId,
        point: point,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Idempotency-Key': idempotencyKey
        },
        timeout: '1s',
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(randomInt(0, 20) / 1000);
}