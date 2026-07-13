#!/usr/bin/env bash
set -euo pipefail

echo "[INFO] Payment Engine automation execution started"
echo "[INFO] Source: MFG-ARC-PAY-CORE-001 v1.0"
echo "[INFO] Scope: Payment Engine only"
echo "[INFO] Repo: ravi-blade5/AIF13July"
echo "[INFO] Branch: payment-engine-sdlc-automation"

mvn clean test
mvn clean verify

echo "[INFO] Payment Engine automation execution completed successfully"
