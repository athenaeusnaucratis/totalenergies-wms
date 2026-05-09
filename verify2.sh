#!/bin/bash
# Register admin (ignore if exists)
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","fullName":"Admin"}' > /dev/null 2>&1

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')

echo "=== Products ==="
curl -s http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(f"{len(d)} products"); [print(f"  {p[\"sku\"]:30s} {p[\"name\"]:35s} {p[\"packaging\"]:10s} S.P=${p[\"sellingPrice\"]}  Margin={p[\"marginPercent\"]}%") for p in d[:5]]'

echo ""
echo "=== Dashboard ==="
curl -s http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo ""
echo "=== Low Stock ==="
curl -s http://localhost:8080/api/dashboard/low-stock \
  -H "Authorization: Bearer $TOKEN" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(f"{len(d)} items below minimum stock")'
