-- Runs once at startup (after Hibernate creates the schema from the @Entity
-- classes, per ddl-auto=create-drop) to seed the H2 database. Order matters:
-- parent rows (customer, product) must exist before child rows that reference
-- them via foreign key (shipping_address, orders, order_item).

-- Customers
INSERT INTO customer (id, displayname, email, phone, birthday) VALUES (1, 'John Smith', 'john.smith@email.com', '555-0101', '1985-03-15');
INSERT INTO customer (id, displayname, email, phone, birthday) VALUES (2, 'Sarah Johnson', 'sarah.johnson@email.com', '555-0102', '1990-07-22');
INSERT INTO customer (id, displayname, email, phone, birthday) VALUES (3, 'Michael Brown', 'michael.brown@email.com', '555-0103', '1988-11-08');

-- Shipping addresses (one-to-one with customer)
INSERT INTO shipping_address (id, address, city, postal_code, country, customer_id) VALUES (1, '123 Main St', 'New York', '10001', 'USA', 1);
INSERT INTO shipping_address (id, address, city, postal_code, country, customer_id) VALUES (2, '456 Oak Ave', 'Los Angeles', '90210', 'USA', 2);
INSERT INTO shipping_address (id, address, city, postal_code, country, customer_id) VALUES (3, '789 Pine Rd', 'Chicago', '60601', 'USA', 3);

-- Products
INSERT INTO product (id, name, price, description, manufacture_date) VALUES (1, 'Wireless Bluetooth Headphones', 79.99, 'Premium quality wireless headphones with noise cancellation and 30-hour battery life', '2024-01-15');
INSERT INTO product (id, name, price, description, manufacture_date) VALUES (2, 'Gaming Mechanical Keyboard', 149.99, 'RGB backlit mechanical keyboard with tactile switches, perfect for gaming and typing', '2024-02-20');
INSERT INTO product (id, name, price, description, manufacture_date) VALUES (3, 'Smartphone Case', 24.99, 'Durable protective case with shock absorption and wireless charging compatibility', '2024-03-10');
INSERT INTO product (id, name, price, description, manufacture_date) VALUES (4, '4K USB Webcam', 89.99, 'High-definition webcam with auto-focus and built-in microphone for video calls', '2024-01-25');

-- Orders (many-to-one with customer)
INSERT INTO orders (id, order_date, status, customer_id) VALUES (1, '2024-06-01', 'COMPLETED', 1);
INSERT INTO orders (id, order_date, status, customer_id) VALUES (2, '2024-06-05', 'PENDING', 2);

-- Order items (many-to-one with order, many-to-one with product)
INSERT INTO order_item (id, quantity, unit_price, order_id, product_id) VALUES (1, 1, 79.99, 1, 1);
INSERT INTO order_item (id, quantity, unit_price, order_id, product_id) VALUES (2, 2, 24.99, 1, 3);
INSERT INTO order_item (id, quantity, unit_price, order_id, product_id) VALUES (3, 1, 149.99, 2, 2);
