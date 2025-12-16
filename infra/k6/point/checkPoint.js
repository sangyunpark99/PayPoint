import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomSeed, randomIntBetween } from 'k6';

randomSeed(1234);

export const options = {
    vus: 100,
    iterations: 100,
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 응답 시간 2초 미만
        http_req_failed: ['rate<0.01'],    // 실패율 1% 미만
    },
};

export default function () {
    const userId = 1;
    const url = `http://spring:8080/api/point/${userId}`;  // ✅ 경로 변수 포함

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '5s',
    };

    const res = http.get(url, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    console.log(`User ${userId} point balance: ${res.body}`);

    sleep(randomIntBetween(50, 150) / 1000);
}