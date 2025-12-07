#!/bin/bash

# Test Paystack Webhook with Proper Signature
# This script computes the correct HMAC signature and sends the webhook

echo "=== Paystack Webhook Test with Proper Signature ==="
echo ""

# Configuration
WEBHOOK_URL="http://localhost:8081/api/webhooks/paystack"
WEBHOOK_SECRET="paystack_test_webhook_secret"  # Must match application.yml

# Get payment reference from user
read -p "Enter payment reference from initiate-booking response: " PAYMENT_REFERENCE

if [ -z "$PAYMENT_REFERENCE" ]; then
    echo "Error: Payment reference is required"
    echo "Example: MTHS-APT-123-1730472123456"
    exit 1
fi

# Create the exact JSON payload
PAYLOAD='{
  "event": "charge.success",
  "data": {
    "id": 302961,
    "domain": "test",
    "status": "success",
    "reference": "'$PAYMENT_REFERENCE'",
    "amount": 1000000,
    "message": null,
    "gateway_response": "Successful",
    "paid_at": "2025-11-02T10:00:00.000Z",
    "created_at": "2025-11-02T09:59:00.000Z",
    "channel": "card",
    "currency": "NGN",
    "ip_address": "127.0.0.1",
    "metadata": "",
    "log": null,
    "fees": 15000,
    "fees_split": null,
    "customer": {
      "id": 1234,
      "first_name": null,
      "last_name": null,
      "email": "patient@example.com",
      "customer_code": "CUS_test123",
      "phone": null,
      "metadata": null,
      "risk_action": "default"
    },
    "authorization": {
      "authorization_code": "AUTH_test123",
      "bin": "408408",
      "last4": "4081",
      "exp_month": "12",
      "exp_year": "2030",
      "channel": "card",
      "card_type": "visa ",
      "bank": "TEST BANK",
      "country_code": "NG",
      "brand": "visa",
      "reusable": true,
      "signature": "SIG_test123",
      "account_name": null
    },
    "plan": {}
  }
}'

echo "Payload:"
echo "$PAYLOAD" | jq '.' 2>/dev/null || echo "$PAYLOAD"
echo ""

# Compute HMAC-SHA512 signature
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha512 -hmac "$WEBHOOK_SECRET" | awk '{print $2}')

echo "Computed HMAC-SHA512 Signature:"
echo "$SIGNATURE"
echo ""
echo "Sending webhook to $WEBHOOK_URL..."
echo ""

# Send webhook with computed signature
HTTP_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -H "x-paystack-signature: $SIGNATURE" \
  -d "$PAYLOAD")

HTTP_STATUS=$(echo "$HTTP_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
RESPONSE_BODY=$(echo "$HTTP_RESPONSE" | sed -e 's/HTTP_STATUS\:.*//g')

echo "=== RESPONSE ==="
echo "Status Code: $HTTP_STATUS"
echo "Response Body:"
echo "$RESPONSE_BODY" | jq '.' 2>/dev/null || echo "$RESPONSE_BODY"
echo ""

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ Webhook processed successfully!"
    echo ""
    echo "Check your database:"
    echo "  SELECT * FROM payments WHERE reference = '$PAYMENT_REFERENCE';"
    echo "  SELECT * FROM appointments WHERE id = (SELECT appointment_id FROM payments WHERE reference = '$PAYMENT_REFERENCE');"
else
    echo "❌ Webhook failed with status $HTTP_STATUS"
fi
