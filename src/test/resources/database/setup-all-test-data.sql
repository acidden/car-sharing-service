INSERT INTO cars (id, model, brand, type, inventory, daily_fee, is_deleted)
VALUES (1, 'Camry', 'Toyota', 'SEDAN', 4, 80.00, false),
       (2, 'X5', 'BMW', 'SUV', 2, 200.00, false);

INSERT INTO users (id, email, password, first_name, last_name, role, is_deleted)
VALUES (1, 'customer@example.com', '$2a$10$PWeguHG1wqkSkiNgPHxEe.XY.9xoKa8U5IBdpEPyVYi3mrjB9GCUq', 'John', 'Doe', 'CUSTOMER', false),
       (2, 'manager@example.com', '$2a$10$PWeguHG1wqkSkiNgPHxEe.XY.9xoKa8U5IBdpEPyVYi3mrjB9GCUq', 'Jane', 'Staff', 'MANAGER', false),
       (3, 'other_customer@example.com', '$2a$10$PWeguHG1wqkSkiNgPHxEe.XY.9xoKa8U5IBdpEPyVYi3mrjB9GCUq', 'Bob', 'Smith', 'CUSTOMER', false);

INSERT INTO users (id, email, password, first_name, last_name, role, is_deleted)
VALUES (10, 'profileuser@example.com', 'pass', 'Alex', 'Smith', 'CUSTOMER', false),
       (11, 'manageruser@example.com', 'pass', 'Jane', 'Staff', 'MANAGER', false),
       (100, 'existinguser@example.com', '$2a$10$PWeguHG1wqkSkiNgPHxEe.XY.9xoKa8U5IBdpEPyVYi3mrjB9GCUq', 'John', 'Doe', 'CUSTOMER', false);

INSERT INTO rentals (id, car_id, user_id, rental_date, return_date)
VALUES (10, 1, 1, '2026-10-10', '2026-10-15'),
       (11, 1, 3, '2026-10-10', '2026-10-15'),
       (12, 1, 1, '2026-10-10', '2026-10-12');

INSERT INTO payments (id, rental_id, session_url, session_id, amount_to_pay, status, type)
VALUES (101, 10, 'https://stripe.com', 'sess_1', 100.00, 'PENDING', 'PAYMENT'),
       (102, 11, 'https://stripe.com', 'sess_2', 250.00, 'PAID', 'FINE'),
       (103, 12, 'https://stripe.com', 'sess_expired_123', 150.00, 'EXPIRED', 'PAYMENT');
