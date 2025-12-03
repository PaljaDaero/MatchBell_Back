# saju_cli.py
# =============================================================================
# Saju 궁합 엔진의 커맨드라인 인터페이스
#
# JSON：
# {
#   "person0": { "year": 2000, "month": 1, "day": 1, "gender": 1 },
#   "person1": { "year": 2001, "month": 2, "day": 3, "gender": 0 }
# }
#
# 응답：
# {
#   "originalScore": ...,
#   "finalScore": ...,
#   "stressScore": ...,
#   "sal0": [...],
#   "sal1": [...],
#   "person0": [...],
#   "person1": [...]
# }
# =============================================================================

import sys
import json
from saju_engine import run_match


def main():
    # 표준 입력에서 JSON 읽기
    raw = sys.stdin.read().strip()
    if not raw:
        print(json.dumps({"error": "empty input"}), flush=True)
        return

    try:
        payload = json.loads(raw)
    except json.JSONDecodeError as e:
        print(json.dumps({"error": f"invalid JSON: {e}"}), flush=True)
        return

    try:
        p0 = payload["person0"]
        p1 = payload["person1"]

        year0 = int(p0["year"])
        month0 = int(p0["month"])
        day0 = int(p0["day"])
        gender0 = int(p0["gender"])  # 1 = 남, 0 = 여

        year1 = int(p1["year"])
        month1 = int(p1["month"])
        day1 = int(p1["day"])
        gender1 = int(p1["gender"])

    except (KeyError, TypeError, ValueError) as e:
        print(json.dumps({"error": f"bad payload: {e}"}), flush=True)
        return

    try:
        result = run_match(
            year0=year0,
            month0=month0,
            day0=day0,
            gender0=gender0,
            year1=year1,
            month1=month1,
            day1=day1,
            gender1=gender1,
        )
    except Exception as e:
        # 실행 중 오류 처리
        print(json.dumps({"error": str(e)}), flush=True)
        return

    print(json.dumps(result, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
