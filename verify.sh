#!/bin/bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo "=== Products count ==="
curl -s http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'{len(d)} products loaded')"

echo ""
echo "=== Sample product ==="
curl -s http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(json.dumps(d[0], indent=2)) if d else print('none')"

echo ""
echo "=== Dashboard ==="
curl -s http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo ""
echo "=== Low stock count ==="
curl -s http://localhost:8080/api/dashboard/low-stock \
  -H "Authorization: Bearer $TOKEN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'{len(d)} items below minimum stock')"
