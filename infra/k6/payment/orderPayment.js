import http from "k6/http";
import { SharedArray } from "k6/data";
import exec from "k6/execution";
import { check } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080/api";

const payables = new SharedArray("payables", () => {
    const text = open("../data/payables.tsv").trim();
    const lines = text
        .split("\n")
        .map((l) => l.trim())
        .filter(Boolean);

    const skipHeader = "true".toLowerCase() === "true";
    const dataLines = skipHeader ? lines.slice(1) : lines;

    const colOffset = 0;

    return dataLines.map((line) => {
        const cols = line.split(/\s+/);
        const orderId = Number(cols[colOffset]);
        const userId = Number(cols[colOffset + 1]);
        return { orderId, userId };
    });
});

export const options = {
    scenarios: {
        // 각 주문을 "정확히 1번" 결제 시도하고 싶을 때 (중복 재사용으로 인한 409/실패 폭증 방지)
        pay_once: {
            executor: "shared-iterations",
            vus: 100,
            iterations: payables.length,
            maxDuration: "30s",
        },
    },
};

export default function () {
    // shared-iterations에서는 iterationInTest가 테스트 전체에서 유일한 인덱스가 됨
    const idx = exec.scenario.iterationInTest % payables.length;
    const { userId, orderId } = payables[idx];

    const payload = JSON.stringify({ userId, orderId });

    // 기본: 주문당 1개 멱등키(동일 주문 재시도는 같은 응답 재사용)
    // 환경변수로 UUID 모드도 가능: IDEM_MODE=uuid
    const idemMode = "uuid";
    const idemKey =
        idemMode === "uuid"
            ? `pay-${Math.random().toString(16).slice(2)}-${Date.now()}-${__VU}-${__ITER}`
            : `pay-${userId}-${orderId}`;

    const res = http.post(`${BASE_URL}/point/payment`, payload, {
        headers: {
            "Content-Type": "application/json",
            "X-Idempotency-Key": idemKey,
        },
    });

    // 실패 원인 확인: 처음 몇 건만 body/status 찍어서 서버 로그 없이도 원인 파악
    const ok = check(res, {
        "status is 2xx/3xx": (r) => r.status >= 200 && r.status < 400,
    });

    if (!ok) {
        const reqId = `${__VU}-${__ITER}-${userId}-${orderId}`;
        console.error(
            JSON.stringify({
                reqId,
                status: res.status,          // 네트워크 실패면 보통 0
                error: res.error,            // 예: "EOF", "request timeout"
                error_code: res.error_code,  // k6 에러 코드
                timings: res.timings,
            })
        );
    }
}