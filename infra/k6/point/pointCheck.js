import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 300,
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
    const userId = randomInt(1, 10000);

    const start = '2025-10-01T00:00:00';
    const end = '2025-11-01T00:00:00';

    const url = `http://spring:8080/api/point/${userId}/histories?startDate=${start}&endDate=${end}`;

    const res = http.get(url, { timeout: '1s' });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}