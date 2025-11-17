import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomSeed } from 'k6';

randomSeed(1234);

export const options = {
    vus: 500,
    duration: '60s',
    thresholds: { // 테스트 성공 판정 기준
        http_req_duration: ['p(95)<2000'], // 응답 시간의 95%가 2초 미만
        http_req_failed: ['rate<0.01'], // 전체 요청 중 실패율이 1% 미만
    },
};

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default function () {
    const userId = randomInt(1, 100);
    const point = randomInt(1, 100) * 10;
    const url = 'http://spring:8080/api/point/charge';

    const payload = JSON.stringify({
        userId: userId,
        point: point,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '5s',
    };

    const res = http.post(url, payload, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(randomInt(50, 150) / 1000);
}