# Payment Orchestration Development Guide

This document outlines the steps to test the payment orchestration flow using the development endpoints provided by `DevPaymentController`.

## Prerequisites

* The application must be running with the `dev` Spring profile active.
    (e.g., `./gradlew bootRun --args='--spring.profiles.active=dev'`)
* A tool like `curl` or Postman to send HTTP requests.

## Step 1: Initiate a New Order

This step creates a new order and sets its status to `AWAITING_PAYMENT`. The response will include the `orderId` which is needed for the next step.

**Endpoint:** `POST /api/dev/payments/initiate`
**Content-Type:** `application/json`

**cURL Command:**

```shell
curl -X POST "http://localhost:8082/api/dev/payments/initiate" \
     -H "Content-Type: application/json" \
     -d 
{
           "customerId": 1,
           "items": [
             {
               "productVariantId": 101,
               "quantity": 2
             },
             {
               "productVariantId": 102,
               "quantity": 1
             }
           ]
         }
```

**Expected Response (example):**

```json
{
  "id": 1,
  "customerId": 1,
  "status": "AWAITING_PAYMENT",
  "items": [
    {
      "id": 1,
      "productVariant": {
        "id": 101,
        "name": "Product A Variant 1",
        "price": 10.00
      },
      "quantity": 2
    },
    {
      "id": 2,
      "productVariant": {
        "id": 102,
        "name": "Product B Variant 1",
        "price": 20.00
      },
      "quantity": 1
    }
  ],
  "totalAmount": 40.00,
  "createdAt": "2025-11-16T10:00:00Z",
  "updatedAt": "2025-11-16T10:00:00Z"
}
```
*Note down the `id` from the response, as this is your `orderId` for the next step.*

## Step 2: Resume SAGA with Cash Payment Confirmation

This step simulates the confirmation of a cash payment for an order that is in `AWAITING_PAYMENT` status. This will trigger the debiting of inventory and marking the order as `PROCESSING`.

**Endpoint:** `POST /api/dev/payments/{orderId}/resume-cash`
**Content-Type:** `application/json`

**cURL Command:**

```bash
# Replace {orderId} with the actual order ID obtained from Step 1
curl -X POST "http://localhost:8082/api/dev/payments/{orderId}/resume-cash" \
     -H "Content-Type: application/json" \
     -d '{ "amount": 150.75 }'
```

**Expected Response:**

A successful response will typically be an empty 200 OK.

---
