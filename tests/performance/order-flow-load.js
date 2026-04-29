import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

// Custom metrics
const orderCreateDuration = new Trend('order_create_duration');
const orderRetrieveDuration = new Trend('order_retrieve_duration');
const orderListDuration = new Trend('order_list_duration');
const orderCreationErrors = new Counter('order_creation_errors');
const orderRetrievalErrors = new Counter('order_retrieval_errors');

// Configuration
const ORDER_INTAKE_URL = __ENV.ORDER_INTAKE_URL || 'http://localhost:8081';

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp-up: 0 to 10 users over 30s
    { duration: '1m', target: 50 },    // Ramp-up: 10 to 50 users over 1m
    { duration: '2m', target: 50 },    // Stay at 50 users for 2m
    { duration: '30s', target: 0 },    // Ramp-down: 50 to 0 users over 30s
  ],
  thresholds: {
    'http_req_duration': ['p(95)<500', 'p(99)<2000'],
    'order_creation_errors': ['count<10'],
    'order_retrieval_errors': ['count<10'],
  },
};

// Helper function to generate unique data
function generateTestOrder() {
  const timestamp = Date.now();
  return {
    customerId: `cust-${Math.random().toString(36).substr(2, 9)}`,
    idempotencyKey: `order-key-${timestamp}-${Math.random().toString(36).substr(2, 9)}`,
    items: [
      {
        sku: `SKU-${Math.floor(Math.random() * 1000)}`,
        quantity: Math.floor(Math.random() * 5) + 1,
        pricePerUnit: Math.floor(Math.random() * 10000) / 100,
      },
    ],
    shippingAddress: {
      street: '123 Test Street',
      city: ['Mumbai', 'Bangalore', 'Delhi', 'Hyderabad'][Math.floor(Math.random() * 4)],
      state: ['MH', 'KA', 'DL', 'TG'][Math.floor(Math.random() * 4)],
      pincode: String(100000 + Math.floor(Math.random() * 900000)),
      country: 'IN',
    },
    paymentMethod: ['CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'COD'][Math.floor(Math.random() * 4)],
  };
}

export default function () {
  // Test Group 1: Create Order
  group('Create Order', () => {
    const payload = JSON.stringify(generateTestOrder());
    
    const createRes = http.post(
      `${ORDER_INTAKE_URL}/api/v1/orders`,
      payload,
      {
        headers: {
          'Content-Type': 'application/json',
        },
        tags: { name: 'CreateOrder' },
      }
    );

    orderCreateDuration.add(createRes.timings.duration);

    const createCheck = check(createRes, {
      'Create order status is 202': (r) => r.status === 202,
      'Create order has orderId': (r) => r.json('orderId') !== null,
      'Create order status is CREATED': (r) => r.json('status') === 'CREATED',
    });

    if (!createCheck) {
      orderCreationErrors.add(1);
      console.log(`Order creation failed: ${createRes.status}`);
    }

    // Extract orderId for subsequent requests
    if (createRes.status === 202 && createRes.json('orderId')) {
      const orderId = createRes.json('orderId');
      sleep(0.1);

      // Test Group 2: Retrieve Order
      group('Retrieve Order', () => {
        const retrieveRes = http.get(
          `${ORDER_INTAKE_URL}/api/v1/orders/${orderId}`,
          {
            tags: { name: 'GetOrder' },
          }
        );

        orderRetrieveDuration.add(retrieveRes.timings.duration);

        const retrieveCheck = check(retrieveRes, {
          'Retrieve order status is 200': (r) => r.status === 200,
          'Retrieve order has same ID': (r) => r.json('orderId') === orderId,
        });

        if (!retrieveCheck) {
          orderRetrievalErrors.add(1);
        }
      });

      sleep(0.1);
    } else {
      orderRetrievalErrors.add(1);
    }
  });

  // Test Group 3: List Orders
  group('List Orders', () => {
    const listRes = http.get(
      `${ORDER_INTAKE_URL}/api/v1/orders?page=1&pageSize=20`,
      {
        tags: { name: 'ListOrders' },
      }
    );

    orderListDuration.add(listRes.timings.duration);

    check(listRes, {
      'List orders status is 200': (r) => r.status === 200,
      'List orders has data array': (r) => Array.isArray(r.json('data')),
    });
  });

  sleep(1);
}
