import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 100,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080/api';
const USER_ID = Number('1');
const PRODUCT_IDS = ('1,2,3').split(',').map(Number);
const QTY = Number('1');

function pickTwoDistinctOrdered(arr) {
    const firstIdx = Math.floor(Math.random() * arr.length);
    const first = arr[firstIdx];

    let second = first;
    while (second === first) {
        const secondIdx = Math.floor(Math.random() * arr.length);
        second = arr[secondIdx];
    }

    return [first, second];
}

export default function () {
    const [p1, p2] = pickTwoDistinctOrdered(PRODUCT_IDS);

    const payload = JSON.stringify({
        userId: USER_ID,
        items: [
            { productId: p1, quantity: randInt(1,3) },
            { productId: p2, quantity: randInt(1,3) },
        ],
    });

    http.post(`${BASE_URL}/orders`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    sleep(0.05);
}

function randInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}