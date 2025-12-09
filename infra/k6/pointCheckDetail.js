import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100,
    duration: '60s',
    thresholds: { // 테스트 성공 판정 기준
        http_req_duration: ['p(95)<800', 'p(99)<1000'],
        http_req_failed: ['rate<0.01'],
    },
};

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default function () {
    const userId = randomInt(1, 3000);

    const start = '2025-10-01T00:00:00';

    const url = `http://spring:8080/api/point/${userId}/detail?startDate=${start}`;

    const res = http.get(url, { timeout: '10s' });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}