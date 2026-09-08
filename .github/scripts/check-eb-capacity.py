"""Read only capacity/installer markers from EB logs; never print raw server logs."""
import json
import subprocess
import time
import urllib.request

ENV = "Ssok-200OK-BE-env"
def aws(*args):
    return json.loads(subprocess.check_output(["aws", "elasticbeanstalk", *args,
        "--region", "ap-northeast-2", "--output", "json"], text=True) or "{}")

try:
    aws("request-environment-info", "--environment-name", ENV, "--info-type", "tail")
    time.sleep(20)
    info = aws("retrieve-environment-info", "--environment-name", ENV, "--info-type", "tail")
    markers = ("Memory total=", "Insufficient capacity:", "Need at least 384 MiB", "Local RabbitMQ ready;", "RabbitMQ did not become ready.")
    for item in info.get("EnvironmentInfo", []):
        with urllib.request.urlopen(item["Message"], timeout=20) as response:
            for line in response.read().decode("utf-8", errors="replace").splitlines():
                if any(marker in line for marker in markers): print(line)
except Exception:
    print("Capacity log retrieval unavailable; inspect Elastic Beanstalk deployment logs.")
