"""
signal_logger.py
Logs every generated signal to a CSV file so you can track
accuracy over time. Works both in Termux testing and inside
the built APK (pass the app's user_data_dir when calling from
inside Kivy so it persists in the right writable location on
Android; falls back to the current folder otherwise).
"""

import os
import csv
from datetime import datetime

LOG_FILENAME = "signal_history.csv"

FIELDNAMES = [
    "timestamp", "price",
    "trend", "momentum", "volatility", "sr", "candlestick", "sentiment",
    "overall", "agreement_pct",
]


def get_log_path(base_dir=None):
    if base_dir is None:
        base_dir = os.getcwd()
    return os.path.join(base_dir, LOG_FILENAME)


def log_signal(groups, overall, agreement, price, base_dir=None):
    """
    Appends one row to the signal history CSV.
    groups: dict like {"trend": "BUY", "momentum": "WAIT", ...}
    overall: "BUY" / "SELL" / "WAIT/MIXED"
    agreement: float percentage (0-100)
    price: current XAU/USD price
    """
    path = get_log_path(base_dir)
    file_exists = os.path.isfile(path)

    row = {
        "timestamp": datetime.now().isoformat(timespec="seconds"),
        "price": round(price, 2),
        "trend": groups.get("trend", ""),
        "momentum": groups.get("momentum", ""),
        "volatility": groups.get("volatility", ""),
        "sr": groups.get("sr", ""),
        "candlestick": groups.get("candlestick", ""),
        "sentiment": groups.get("sentiment", ""),
        "overall": overall,
        "agreement_pct": round(agreement, 1),
    }

    with open(path, "a", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=FIELDNAMES)
        if not file_exists:
            writer.writeheader()
        writer.writerow(row)

    return path
