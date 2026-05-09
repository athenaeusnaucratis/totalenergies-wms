#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')
echo "Token: ${TOKEN:0:30}..."

for ep in dashboard gl-accounts stock-locations stock-items purchase-orders sales-orders invoices reports/trial-balance reports/profit-loss reports/profit-per-item; do
  CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/$ep" -H "Authorization: Bearer $TOKEN")
  echo "$ep => HTTP $CODE"
done
