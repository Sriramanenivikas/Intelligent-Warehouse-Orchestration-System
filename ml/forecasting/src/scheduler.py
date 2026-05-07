from __future__ import annotations

import os
import time
from datetime import datetime, timedelta, timezone

from train_and_publish import run_training_pipeline


def next_run_epoch(interval_seconds: int) -> float:
    now = datetime.now(timezone.utc)
    elapsed = int(now.timestamp())
    next_epoch = ((elapsed // interval_seconds) + 1) * interval_seconds
    return float(next_epoch)


def main() -> None:
    interval_seconds = int(os.getenv("FORECASTING_TRAINING_INTERVAL_SECONDS", "900"))
    while True:
        started_at = datetime.now(timezone.utc)
        print(f"[forecasting-trainer] starting run at {started_at.isoformat()}")
        try:
            result = run_training_pipeline()
            print(f"[forecasting-trainer] completed run: {result}")
        except Exception as exception:
            print(f"[forecasting-trainer] run failed: {exception}")
        sleep_until = next_run_epoch(interval_seconds)
        delay = max(1.0, sleep_until - time.time())
        next_run_at = datetime.fromtimestamp(sleep_until, tz=timezone.utc)
        print(f"[forecasting-trainer] next run at {next_run_at.isoformat()}")
        time.sleep(delay)


if __name__ == "__main__":
    main()
