#!/bin/bash

# Test sync API with a sample request
echo "Testing sync API..."

# Test data - a sample recipe to sync
curl -X POST http://localhost:8080/api/sync \
  -H "Content-Type: application/json" \
  -d '{
    "entities": [
      {
        "localId": "test-recipe-001",
        "serverId": null,
        "type": "RECIPE",
        "data": {
          "name": "Test Recipe from Frontend"
        },
        "version": 1,
        "timestamp": 1768439000000,
        "checksum": "abc123"
      },
      {
        "localId": "test-ingredient-001",
        "serverId": null,
        "type": "INGREDIENT",
        "data": {
          "name": "Test Ingredient"
        },
        "version": 1,
        "timestamp": 1768439000000,
        "checksum": "def456"
      }
    ]
  }' | jq .

echo ""
echo "Sync test completed!"