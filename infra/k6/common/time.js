export function formatKst(date) {
    // KST = UTC+9
    const kst = new Date(date.getTime() + 9 * 60 * 60 * 1000);

    const y  = kst.getUTCFullYear();
    const m  = String(kst.getUTCMonth() + 1).padStart(2, '0');
    const d  = String(kst.getUTCDate()).padStart(2, '0');
    const hh = String(kst.getUTCHours()).padStart(2, '0');
    const mm = String(kst.getUTCMinutes()).padStart(2, '0');
    const ss = String(kst.getUTCSeconds()).padStart(2, '0');

    return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
}

export function baseSetup() {
    const start = new Date();
    console.log(`🚀 TEST START (KST): ${formatKst(start)}`);
    return { startTimeMs: start.getTime() };
}

export function baseTeardown(data) {
    const end = new Date();
    console.log(`🏁 TEST END   (KST): ${formatKst(end)}`);

    const elapsedSec = (end.getTime() - data.startTimeMs) / 1000;
    console.log(`⏱ TOTAL DURATION: ${elapsedSec.toFixed(2)}s`);
}