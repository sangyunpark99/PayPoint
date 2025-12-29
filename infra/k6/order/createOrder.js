import http from 'k6/http';
import { sleep } from 'k6';

const BASE_URL = 'http://localhost:8080/api';

const USER_MIN = Number(1);
const USER_MAX = Number(10000);

const PRODUCT_MIN = Number(1);
const PRODUCT_MAX = Number(50000);

const ITEMS_PER_ORDER = Number(10);

const QTY_MIN = Number(1);
const QTY_MAX = Number(3);

const SLEEP_SEC = Number(0.05);

export const options = {
  vus: Number(100),
  duration: '30s',
};

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pickDistinctInts(count, min, max) {
  const s = new Set();
  while (s.size < count) {
    s.add(randInt(min, max));
  }
  return Array.from(s);
}

export default function () {
  const userId = randInt(USER_MIN, USER_MAX);

  const productIds = pickDistinctInts(ITEMS_PER_ORDER, PRODUCT_MIN, PRODUCT_MAX);
  const items = productIds.map((pid) => ({
    productId: pid,
    quantity: randInt(QTY_MIN, QTY_MAX),
  }));

  const payload = JSON.stringify({
    userId,
    items,
  });

  http.post(`${BASE_URL}/orders`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /orders' },
  });

  sleep(SLEEP_SEC);
}