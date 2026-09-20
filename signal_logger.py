"""
signal_logger.py
Logs every generated signal to a CSV file so you can track
accuracy over time. Works both in Termux testing and inside
the built APK (pass the app's user_data_dir when calling from
inside Kivy so it persists in the right writable location on
Android; falls back to the current folder otherwise).

Also provides:
  - export_log(): copies the CSV to Android's app-specific
    external storage folder (Android/data/<package>/files/).
    NOTE: on Android 11+, this folder is not browsable by other
    apps (file managers included) even with storage permission
    granted - that's an OS restriction, not a bug here.
  - share_log(): opens Android's native share sheet with the
    log content as plain text, so it can be sent via Telegram,
    WhatsApp, email, etc. This is the reliable way to actually
    get the data off the device.
"""

import os
import csv
import shutil
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


def get_export_dir():
    """
    Returns Android's app-specific external files directory
    (Android/data/<package>/files/). Falls back to the current
    directory when not running on Android.
    """
    try:
        from jnius import autoclass
        PythonActivity = autoclass("org.kivy.android.PythonActivity")
        context = PythonActivity.mActivity
        export_dir = context.getExternalFilesDir(None).getAbsolutePath()
        return export_dir
    except Exception:
        return os.getcwd()


def export_log(base_dir=None):
    """
    Copies the current signal_history.csv to the app-specific
    external storage folder. Safe to call often. Returns the
    export path, or None if the log file doesn't exist yet.
    """
    source_path = get_log_path(base_dir)
    if not os.path.isfile(source_path):
        return None

    export_dir = get_export_dir()
    os.makedirs(export_dir, exist_ok=True)
    export_path = os.path.join(export_dir, LOG_FILENAME)

    shutil.copyfile(source_path, export_path)
    return export_path


def share_log(base_dir=None, max_lines=150):
    """
    Opens Android's native share sheet with the most recent
    max_lines rows of the signal history as plain text, so it
    can be sent via Telegram, WhatsApp, email, saved to Drive,
    etc. This works regardless of the Android/data restriction,
    since the app is sharing its own data directly.

    Returns True if the share sheet was launched, False otherwise
    (e.g. no log file yet, or not running on Android).
    """
    path = get_log_path(base_dir)
    if not os.path.isfile(path):
        return False

    with open(path, "r") as f:
        lines = f.readlines()

    if not lines:
        return False

    header = lines[0]
    body_lines = lines[1:]
    recent_lines = body_lines[-max_lines:]
    text = header + "".join(recent_lines)

    try:
        from jnius import autoclass, cast
        PythonActivity = autoclass("org.kivy.android.PythonActivity")
        Intent = autoclass("android.content.Intent")
        String = autoclass("java.lang.String")

        intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, String("Signal History"))
        intent.putExtra(Intent.EXTRA_TEXT, String(text))

        chooser = Intent.createChooser(
            intent, cast("java.lang.CharSequence", String("Share signal history"))
        )
        PythonActivity.mActivity.startActivity(chooser)
        return True
    except Exception as e:
        print(f"[signal_logger] Share failed: {e}")
        return False
