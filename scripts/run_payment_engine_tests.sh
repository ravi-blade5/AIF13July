#!/usr/bin/env bash
set -euo pipefail

echo "[INFO] Starting Payment Engine test execution"
echo "[INFO] Source: MFG-ARC-PAY-CORE-001 v1.0"
echo "[INFO] Scope: Payment Engine only"

mvn clean test
mvn clean verify

echo "[INFO] Test execution completed successfully"
