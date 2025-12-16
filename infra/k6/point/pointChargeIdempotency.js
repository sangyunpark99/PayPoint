import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
};

const fixedUserId = __VU;             // VU마다 고정
const fixedPoint = 100;
const fixedKey = `fixed-${__VU}`;     // VU마다 고정 멱등키

export default function () {
    let userId, point, idempotencyKey;

    if (Math.random() < 0.2) {
        userId = fixedUserId;
        point = fixedPoint;
        idempotencyKey = fixedKey;
    } else {
        userId = Math.floor(Math.random() * 1000) + 1;
        point = 100;
        idempotencyKey = crypto.randomUUID();
    }

    const res = http.post(
        'http://localhost:8080/api/point/charge',
        JSON.stringify({ userId, point }),
        {
            headers: {
                'Content-Type': 'application/json',
                'X-Idempotency-Key': idempotencyKey,
            },
            timeout: '2s',
        }
    );

    check(res, { 'status is 200': (r) => r.status === 200 });
}