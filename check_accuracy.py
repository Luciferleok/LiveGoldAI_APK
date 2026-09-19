"""
check_accuracy.py
Run this anytime to see how past signals performed.

Compares each logged signal's price to the price N rows later
(each row = one app refresh, roughly 60 seconds apart while the
app was running) to see whether BUY signals were followed by a
price increase, and SELL signals by a decrease.

Usage:
    python check_accuracy.py
    python check_accuracy.py 10      # compare 10 refreshes ahead instead of default 5
"""

import csv
import sys
from signal_logger import get_log_path


def load_rows(path):
    rows = []
    with open(path, newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            row["price"] = float(row["price"])
            row["agreement_pct"] = float(row["agreement_pct"])
            rows.append(row)
    return rows


def check_accuracy(rows, lookahead=5):
    results = {"BUY": [0, 0], "SELL": [0, 0]}  # [correct, total]

    for i in range(len(rows) - lookahead):
        current = rows[i]
        future = rows[i + lookahead]
        signal = current["overall"]

        if signal not in results:
            continue  # skips WAIT/MIXED, not scored

        price_now = current["price"]
        price_future = future["price"]

        if signal == "BUY":
            correct = price_future > price_now
        else:  # SELL
            correct = price_future < price_now

        results[signal][1] += 1
        if correct:
            results[signal][0] += 1

    return results


if __name__ == "__main__":
    lookahead = int(sys.argv[1]) if len(sys.argv) > 1 else 5
    path = get_log_path()

    try:
        rows = load_rows(path)
    except FileNotFoundError:
        print(f"No log file found yet at {path}")
        print("Run the app for a while first so it can collect signal history.")
        sys.exit(1)

    print(f"Loaded {len(rows)} logged signals from {path}")
    print(f"Comparing each signal to price {lookahead} refreshes later\n")

    results = check_accuracy(rows, lookahead)

    for signal, (correct, total) in results.items():
        if total == 0:
            print(f"{signal}: no data yet")
            continue
        pct = (correct / total) * 100
        print(f"{signal}: {correct}/{total} correct ({pct:.1f}%)")
