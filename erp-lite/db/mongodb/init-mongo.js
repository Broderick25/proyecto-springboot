// ERP LITE - MongoDB Initialization Script
// Database: erp_catalog_db
// Collections: catalogs, product_documents, audit_logs

// Switch database
db = db.getSiblingDB('erp_catalog_db');

// -----------------------------
// COLLECTION: catalogs
// -----------------------------
db.createCollection('catalogs');

db.catalogs.insertMany([
  {
    _id: 'catalog-product-categories',
    catalogType: 'PRODUCT_CATEGORIES',
    name: 'Product Categories',
    description: 'Main product categories for the ERP system',
    active: true,
    items: [
      {
        id: 'cat-electronics',
        code: 'ELECTRONICS',
        value: 'Electronics',
        description: 'Electronic devices and accessories',
        displayOrder: 1,
        metadata: { icon: 'laptop', color: '#2196F3' }
      },
      {
        id: 'cat-furniture',
        code: 'FURNITURE',
        value: 'Furniture',
        description: 'Office and home furniture',
        displayOrder: 2,
        metadata: { icon: 'chair', color: '#4CAF50' }
      },
      {
        id: 'cat-accessories',
        code: 'ACCESSORIES',
        value: 'Accessories',
        description: 'Various accessories and peripherals',
        displayOrder: 3,
        metadata: { icon: 'backpack', color: '#FF9800' }
      },
      {
        id: 'cat-stationery',
        code: 'STATIONERY',
        value: 'Stationery',
        description: 'Office supplies and stationery',
        displayOrder: 4,
        metadata: { icon: 'pencil', color: '#9C27B0' }
      }
    ],
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'catalog-order-statuses',
    catalogType: 'ORDER_STATUSES',
    name: 'Order Statuses',
    description: 'Possible statuses for orders',
    active: true,
    items: [
      {
        id: 'status-pending',
        code: 'PENDING',
        value: 'Pending',
        description: 'Order created, awaiting confirmation',
        displayOrder: 1,
        metadata: { color: '#FFC107', nextStatuses: ['CONFIRMED', 'CANCELLED'] }
      },
      {
        id: 'status-confirmed',
        code: 'CONFIRMED',
        value: 'Confirmed',
        description: 'Order confirmed, ready for shipment',
        displayOrder: 2,
        metadata: { color: '#2196F3', nextStatuses: ['SHIPPED', 'CANCELLED'] }
      },
      {
        id: 'status-shipped',
        code: 'SHIPPED',
        value: 'Shipped',
        description: 'Order shipped',
        displayOrder: 3,
        metadata: { color: '#FF9800', nextStatuses: ['DELIVERED'] }
      },
      {
        id: 'status-delivered',
        code: 'DELIVERED',
        value: 'Delivered',
        description: 'Order delivered successfully',
        displayOrder: 4,
        metadata: { color: '#4CAF50', nextStatuses: [] }
      },
      {
        id: 'status-cancelled',
        code: 'CANCELLED',
        value: 'Cancelled',
        description: 'Order cancelled',
        displayOrder: 5,
        metadata: { color: '#F44336', nextStatuses: [] }
      }
    ],
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'catalog-payment-methods',
    catalogType: 'PAYMENT_METHODS',
    name: 'Payment Methods',
    description: 'Available payment methods',
    active: true,
    items: [
      {
        id: 'pay-credit-card',
        code: 'CREDIT_CARD',
        value: 'Credit Card',
        description: 'Visa, Mastercard, American Express',
        displayOrder: 1,
        metadata: { icon: 'credit-card', fee: 2.9 }
      },
      {
        id: 'pay-paypal',
        code: 'PAYPAL',
        value: 'PayPal',
        description: 'PayPal account',
        displayOrder: 2,
        metadata: { icon: 'paypal', fee: 3.4 }
      },
      {
        id: 'pay-bank-transfer',
        code: 'BANK_TRANSFER',
        value: 'Bank Transfer',
        description: 'Direct bank transfer',
        displayOrder: 3,
        metadata: { icon: 'bank', fee: 0 }
      },
      {
        id: 'pay-cash',
        code: 'CASH',
        value: 'Cash',
        description: 'Cash on delivery',
        displayOrder: 4,
        metadata: { icon: 'money', fee: 0 }
      }
    ],
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'catalog-shipping-methods',
    catalogType: 'SHIPPING_METHODS',
    name: 'Shipping Methods',
    description: 'Available shipping options',
    active: true,
    items: [
      {
        id: 'ship-standard',
        code: 'STANDARD',
        value: 'Standard Shipping',
        description: '5-7 business days',
        displayOrder: 1,
        metadata: { icon: 'truck', cost: 9.99, estimatedDays: 7 }
      },
      {
        id: 'ship-express',
        code: 'EXPRESS',
        value: 'Express Shipping',
        description: '2-3 business days',
        displayOrder: 2,
        metadata: { icon: 'rocket', cost: 24.99, estimatedDays: 3 }
      },
      {
        id: 'ship-overnight',
        code: 'OVERNIGHT',
        value: 'Overnight',
        description: 'Next business day',
        displayOrder: 3,
        metadata: { icon: 'zap', cost: 39.99, estimatedDays: 1 }
      },
      {
        id: 'ship-pickup',
        code: 'PICKUP',
        value: 'Store Pickup',
        description: 'Pick up at store',
        displayOrder: 4,
        metadata: { icon: 'store', cost: 0, estimatedDays: 0 }
      }
    ],
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'catalog-countries',
    catalogType: 'COUNTRIES',
    name: 'Countries',
    description: 'Supported countries for shipping',
    active: true,
    items: [
      {
        id: 'country-us',
        code: 'US',
        value: 'United States',
        description: 'United States of America',
        displayOrder: 1,
        metadata: { flag: '🇺🇸', currency: 'USD', phonePrefix: '+1' }
      },
      {
        id: 'country-mx',
        code: 'MX',
        value: 'Mexico',
        description: 'Mexico',
        displayOrder: 2,
        metadata: { flag: '🇲🇽', currency: 'MXN', phonePrefix: '+52' }
      },
      {
        id: 'country-ca',
        code: 'CA',
        value: 'Canada',
        description: 'Canada',
        displayOrder: 3,
        metadata: { flag: '🇨🇦', currency: 'CAD', phonePrefix: '+1' }
      },
      {
        id: 'country-uk',
        code: 'UK',
        value: 'United Kingdom',
        description: 'United Kingdom',
        displayOrder: 4,
        metadata: { flag: '🇬🇧', currency: 'GBP', phonePrefix: '+44' }
      },
      {
        id: 'country-de',
        code: 'DE',
        value: 'Germany',
        description: 'Germany',
        displayOrder: 5,
        metadata: { flag: '🇩🇪', currency: 'EUR', phonePrefix: '+49' }
      }
    ],
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'catalog-currencies',
    catalogType: 'CURRENCIES',
    name: 'Currencies',
    description: 'Supported currencies',
    active: true,
    items: [
      { id: 'curr-usd', code: 'USD', value: 'US Dollar', description: 'United States Dollar', displayOrder: 1, metadata: { symbol: '$', decimalPlaces: 2 } },
      { id: 'curr-mxn', code: 'MXN', value: 'Mexican Peso', description: 'Mexican Peso', displayOrder: 2, metadata: { symbol: '$', decimalPlaces: 2 } },
      { id: 'curr-eur', code: 'EUR', value: 'Euro', description: 'Euro', displayOrder: 3, metadata: { symbol: '€', decimalPlaces: 2 } },
      { id: 'curr-gbp', code: 'GBP', value: 'British Pound', description: 'British Pound Sterling', displayOrder: 4, metadata: { symbol: '£', decimalPlaces: 2 } },
      { id: 'curr-cad', code: 'CAD', value: 'Canadian Dollar', description: 'Canadian Dollar', displayOrder: 5, metadata: { symbol: '$', decimalPlaces: 2 } }
    ],
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  }
]);

// -----------------------------
// COLLECTION: product_documents
// -----------------------------
db.createCollection('product_documents');

db.product_documents.insertMany([
  // 10 product documents (idénticos a los tuyos, solo identados)
  {
    _id: '11111111-1111-1111-1111-111111111111',
    sku: 'LAPTOP-001',
    name: 'Laptop Dell XPS 15',
    description: 'High-performance laptop with Intel i7, 16GB RAM, 512GB SSD',
    price: 1499.99,
    currency: 'USD',
    stock: 25,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['laptop', 'dell', 'xps', 'intel', 'portable'],
    specifications: {
      processor: 'Intel Core i7-13700H',
      ram: '16GB DDR5',
      storage: '512GB NVMe SSD',
      display: '15.6" FHD',
      weight: '2.0 kg'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '22222222-2222-2222-2222-222222222222',
    sku: 'LAPTOP-002',
    name: 'MacBook Pro 14"',
    description: 'Apple M3 Pro chip, 18GB RAM, 512GB SSD',
    price: 1999.99,
    currency: 'USD',
    stock: 15,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['laptop', 'apple', 'macbook', 'm3', 'pro'],
    specifications: {
      processor: 'Apple M3 Pro',
      ram: '18GB Unified Memory',
      storage: '512GB SSD',
      display: '14.2" Liquid Retina XDR',
      weight: '1.6 kg'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },

  {
    _id: '33333333-3333-3333-3333-333333333333',
    sku: 'DESK-001',
    name: 'Standing Desk Electric',
    description: 'Adjustable height standing desk, 120x60cm',
    price: 499.99,
    currency: 'USD',
    stock: 30,
    categoryId: 'cat-furniture',
    categoryName: 'Furniture',
    imageUrl: null,
    active: true,
    tags: ['desk', 'standing', 'electric', 'adjustable', 'ergonomic'],
    specifications: {
      dimensions: '120x60cm',
      heightRange: '70-120cm',
      weightCapacity: '80kg',
      material: 'MDF + Steel',
      motor: 'Dual motor'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '44444444-4444-4444-4444-444444444444',
    sku: 'CHAIR-001',
    name: 'Ergonomic Office Chair',
    description: 'Herman Miller Aeron, size B, fully adjustable',
    price: 1299.99,
    currency: 'USD',
    stock: 12,
    categoryId: 'cat-furniture',
    categoryName: 'Furniture',
    imageUrl: null,
    active: true,
    tags: ['chair', 'ergonomic', 'office', 'herman miller', 'aeron'],
    specifications: {
      brand: 'Herman Miller',
      model: 'Aeron Size B',
      material: 'Mesh',
      adjustable: 'Yes',
      maxWeight: '136kg'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '55555555-5555-5555-5555-555555555555',
    sku: 'MONITOR-001',
    name: 'Dell UltraSharp 27"',
    description: '4K IPS monitor, USB-C, 99% sRGB',
    price: 599.99,
    currency: 'USD',
    stock: 40,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['monitor', 'dell', '4k', 'ips', 'ultrasharp'],
    specifications: {
      size: '27 inch',
      resolution: '3840x2160 4K',
      panel: 'IPS',
      connectivity: 'USB-C, HDMI, DisplayPort',
      colorGamut: '99% sRGB'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '66666666-6666-6666-6666-666666666666',
    sku: 'KEYBOARD-001',
    name: 'Mechanical Keyboard RGB',
    description: 'Cherry MX Brown switches, aluminum frame',
    price: 149.99,
    currency: 'USD',
    stock: 50,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['keyboard', 'mechanical', 'rgb', 'cherry mx', 'gaming'],
    specifications: {
      switches: 'Cherry MX Brown',
      layout: 'TKL',
      frame: 'Aluminum',
      backlight: 'RGB',
      connectivity: 'USB'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '77777777-7777-7777-7777-777777777777',
    sku: 'MOUSE-001',
    name: 'Logitech MX Master 3S',
    description: 'Wireless mouse, ergonomic, 8K DPI',
    price: 99.99,
    currency: 'USD',
    stock: 60,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['mouse', 'logitech', 'wireless', 'ergonomic', 'mx master'],
    specifications: {
      dpi: '8000',
      buttons: '7',
      connectivity: 'Bluetooth + USB',
      battery: '70 days',
      weight: '141g'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '88888888-8888-8888-8888-888888888888',
    sku: 'PRINTER-001',
    name: 'HP LaserJet Pro',
    description: 'Black & white laser printer, duplex, Wi-Fi',
    price: 299.99,
    currency: 'USD',
    stock: 20,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['printer', 'hp', 'laser', 'monochrome', 'duplex', 'wifi'],
    specifications: {
      type: 'Laser monochrome',
      speed: '33 ppm',
      connectivity: 'Wi-Fi, USB, Ethernet',
      duplex: 'Yes',
      resolution: '1200 dpi'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '99999999-9999-9999-9999-999999999999',
    sku: 'TABLET-001',
    name: 'iPad Air 11"',
    description: 'M2 chip, 128GB, Wi-Fi, Space Gray',
    price: 599.99,
    currency: 'USD',
    stock: 35,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['tablet', 'apple', 'ipad', 'm2', 'ios'],
    specifications: {
      processor: 'Apple M2',
      storage: '128GB',
      display: '11" Liquid Retina',
      connectivity: 'Wi-Fi',
      color: 'Space Gray'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    sku: 'LAMP-001',
    name: 'LED Desk Lamp',
    description: 'Adjustable brightness, USB charging port',
    price: 49.99,
    currency: 'USD',
    stock: 80,
    categoryId: 'cat-furniture',
    categoryName: 'Furniture',
    imageUrl: null,
    active: true,
    tags: ['lamp', 'led', 'desk', 'adjustable', 'usb'],
    specifications: {
      type: 'LED',
      brightness: '5 levels',
      colorTemp: '3000K-6000K',
      usbPort: 'Yes',
      material: 'Aluminum'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    sku: 'WEBCAM-001',
    name: 'Logitech C920 HD Pro',
    description: '1080p video, stereo audio, autofocus',
    price: 79.99,
    currency: 'USD',
    stock: 45,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['webcam', 'logitech', '1080p', 'autofocus', 'streaming'],
    specifications: {
      resolution: '1080p Full HD',
      fps: '30fps',
      microphone: 'Stereo',
      autofocus: 'Yes',
      connectivity: 'USB'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'cccccccc-cccc-cccc-cccc-cccccccccccc',
    sku: 'HEADSET-001',
    name: 'Sony WH-1000XM5',
    description: 'Noise cancelling, 30h battery, Bluetooth',
    price: 399.99,
    currency: 'USD',
    stock: 28,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['headset', 'sony', 'noise-cancelling', 'wireless', 'bluetooth'],
    specifications: {
      type: 'Over-ear',
      battery: '30 hours',
      connectivity: 'Bluetooth 5.2',
      noiseCancelling: 'Yes',
      weight: '250g'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'dddddddd-dddd-dddd-dddd-dddddddddddd',
    sku: 'DOCK-001',
    name: 'CalDigit TS4',
    description: 'Thunderbolt 4 dock, 18 ports, 98W power delivery',
    price: 399.99,
    currency: 'USD',
    stock: 18,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['dock', 'thunderbolt', 'caldigit', 'hub', 'usb-c'],
    specifications: {
      interface: 'Thunderbolt 4',
      ports: 18,
      powerDelivery: '98W',
      videoOutput: '2x Thunderbolt, 1x DisplayPort',
      connectivity: 'USB-C'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
    sku: 'CABLE-001',
    name: 'USB-C Cable 2m',
    description: 'Thunderbolt 4, 40Gbps, 100W charging',
    price: 29.99,
    currency: 'USD',
    stock: 100,
    categoryId: 'cat-electronics',
    categoryName: 'Electronics',
    imageUrl: null,
    active: true,
    tags: ['cable', 'usb-c', 'thunderbolt', 'charging', '100w'],
    specifications: {
      length: '2m',
      standard: 'Thunderbolt 4',
      dataTransfer: '40Gbps',
      charging: '100W',
      compatibility: 'USB4'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: 'ffffffff-ffff-ffff-ffff-ffffffffffff',
    sku: 'BACKPACK-001',
    name: 'Laptop Backpack',
    description: 'Water-resistant, fits 15" laptop, USB charging port',
    price: 69.99,
    currency: 'USD',
    stock: 55,
    categoryId: 'cat-accessories',
    categoryName: 'Accessories',
    imageUrl: null,
    active: true,
    tags: ['backpack', 'laptop', 'travel', 'waterproof', 'usb'],
    specifications: {
      capacity: '25L',
      laptopSize: 'Up to 15 inch',
      material: 'Water-resistant polyester',
      usbPort: 'External charging',
      color: 'Black'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '10101010-1010-1010-1010-101010101010',
    sku: 'ORGANIZER-001',
    name: 'Desktop Organizer',
    description: 'Bamboo wood, 5 compartments, cable management',
    price: 39.99,
    currency: 'USD',
    stock: 70,
    categoryId: 'cat-furniture',
    categoryName: 'Furniture',
    imageUrl: null,
    active: true,
    tags: ['organizer', 'desk', 'bamboo', 'cable management', 'office'],
    specifications: {
      material: 'Bamboo',
      compartments: 5,
      cableManagement: 'Yes',
      dimensions: '30x20x10cm',
      color: 'Natural'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '20202020-2020-2020-2020-202020202020',
    sku: 'PLANT-001',
    name: 'Artificial Desk Plant',
    description: 'Small succulent, realistic, no maintenance',
    price: 19.99,
    currency: 'USD',
    stock: 90,
    categoryId: 'cat-accessories',
    categoryName: 'Accessories',
    imageUrl: null,
    active: true,
    tags: ['plant', 'artificial', 'desk', 'decor', 'succulent'],
    specifications: {
      type: 'Succulent',
      material: 'Plastic + Ceramic pot',
      height: '15cm',
      maintenance: 'None',
      color: 'Green'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '30303030-3030-3030-3030-303030303030',
    sku: 'NOTEBOOK-001',
    name: 'Moleskine Classic Notebook',
    description: 'Large, ruled, hardcover, 240 pages',
    price: 24.99,
    currency: 'USD',
    stock: 75,
    categoryId: 'cat-stationery',
    categoryName: 'Stationery',
    imageUrl: null,
    active: true,
    tags: ['notebook', 'moleskine', 'ruled', 'hardcover', 'writing'],
    specifications: {
      size: 'Large (13x21cm)',
      pages: 240,
      ruling: 'Ruled',
      cover: 'Hardcover',
      color: 'Black'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '40404040-4040-4040-4040-404040404040',
    sku: 'PEN-001',
    name: 'Pilot G2 Gel Pens Pack',
    description: 'Pack of 12, 0.7mm, black ink',
    price: 14.99,
    currency: 'USD',
    stock: 120,
    categoryId: 'cat-stationery',
    categoryName: 'Stationery',
    imageUrl: null,
    active: true,
    tags: ['pen', 'pilot', 'gel', 'ink', 'writing'],
    specifications: {
      quantity: 12,
      tipSize: '0.7mm',
      inkColor: 'Black',
      type: 'Gel',
      refillable: 'Yes'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  },
  {
    _id: '50505050-5050-5050-5050-505050505050',
    sku: 'WHITEBOARD-001',
    name: 'Magnetic Whiteboard',
    description: '90x60cm, aluminum frame, includes markers',
    price: 89.99,
    currency: 'USD',
    stock: 22,
    categoryId: 'cat-furniture',
    categoryName: 'Furniture',
    imageUrl: null,
    active: true,
    tags: ['whiteboard', 'magnetic', 'office', 'presentation', 'aluminum'],
    specifications: {
      dimensions: '90x60cm',
      frame: 'Aluminum',
      surface: 'Magnetic',
      includes: 'Markers + eraser',
      mounting: 'Wall mount'
    },
    createdAt: new Date('2025-01-01T00:00:00Z'),
    updatedAt: new Date('2025-01-01T00:00:00Z')
  }
]);

// -----------------------------
// COLLECTION: audit_logs
// -----------------------------
db.createCollection('audit_logs');

db.audit_logs.insertOne({
  _id: new ObjectId(),
  className: 'CreateOrderUseCase',
  methodName: 'execute',
  userId: 'admin',
  timestamp: new Date(),
  executionTimeMs: 127,
  success: true,
  errorMessage: null,
  ipAddress: '127.0.0.1',
  endpoint: '/api/v1/orders'
});

// -----------------------------
// INDEXES
// -----------------------------
db.audit_logs.createIndex({ timestamp: -1 });
db.audit_logs.createIndex({ className: 1, methodName: 1 });
db.audit_logs.createIndex({ userId: 1 });
db.audit_logs.createIndex({ success: 1 });

db.catalogs.createIndex({ catalogType: 1 });
db.catalogs.createIndex({ active: 1 });
db.catalogs.createIndex({ 'items.code': 1 });

db.product_documents.createIndex({ sku: 1 }, { unique: true });
db.product_documents.createIndex({ categoryId: 1 });
db.product_documents.createIndex({ active: 1 });
db.product_documents.createIndex({ tags: 1 });
db.product_documents.createIndex({ name: 'text', description: 'text' });
db.product_documents.createIndex({ price: 1 });
db.product_documents.createIndex({ stock: 1 });

// -----------------------------
// SUMMARY
// -----------------------------
print('\n==============================================');
print('ERP LITE - MongoDB Initialization Complete');
print('----------------------------------------------');
print('Database: erp_catalog_db');
print('Catalogs: ' + db.catalogs.countDocuments());
print('Products: ' + db.product_documents.countDocuments());
print('Audit Logs: ' + db.audit_logs.countDocuments());
print('==============================================\n');