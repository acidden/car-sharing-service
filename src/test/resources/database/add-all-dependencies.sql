SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payments;
TRUNCATE TABLE rentals;
TRUNCATE TABLE cars;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. ПОЛЬЗОВАТЕЛИ
-- Пользователь с ID 1 (имеет активные, завершенные и просроченные аренды, а также платежи)
INSERT INTO users (id, email, first_name, last_name, password, role, is_deleted)
VALUES (1, 'customer@example.com', 'Bob', 'Alison', 'password123', 'CUSTOMER', false);

-- Пользователь с ID 2 (чистый, без аренд и платежей — для проверки негативных сценариев)
INSERT INTO users (id, email, first_name, last_name, password, role, is_deleted)
VALUES (2, 'clean@example.com', 'Alice', 'Smith', 'password123', 'CUSTOMER', false);


-- 2. АВТОМОБИЛИ
INSERT INTO cars (id, brand, model, type, inventory, daily_fee, is_deleted)
VALUES (1, 'Tesla', 'Model Y', 'SUV', 3, 120.00, false);

INSERT INTO cars (id, brand, model, type, inventory, daily_fee, is_deleted)
VALUES (2, 'BMW', 'X5', 'SUV', 1, 150.00, false);


-- 3. АРЕНДЫ (для RentalRepository)
-- Аренда 1: Активная (не возвращена, actual_return_date IS NULL)
INSERT INTO rentals (id, user_id, car_id, rental_date, return_date, actual_return_date)
VALUES (1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), NULL);

-- Аренда 2: Завершенная (вернули вовремя)
INSERT INTO rentals (id, user_id, car_id, rental_date, return_date, actual_return_date)
VALUES (2, 1, 2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY));

-- Аренда 3: Просроченная (дата возврата прошла, actual_return_date равен NULL)
INSERT INTO rentals (id, user_id, car_id, rental_date, return_date, actual_return_date)
VALUES (3, 1, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), NULL);


-- 4. ПЛАТЕЖИ (для PaymentRepository)
-- Платеж 1: Ожидает оплаты, привязан к активной аренде (ID 1)
INSERT INTO payments (id, rental_id, session_id, session_url, amount_to_pay, status, type)
VALUES (1, 1, 'test_session_123_xyz', 'https://stripe.com', 360.00, 'PENDING', 'PAYMENT');

-- Платеж 2: Оплачен, привязан к завершенной аренде (ID 2)
INSERT INTO payments (id, rental_id, session_id, session_url, amount_to_pay, status, type)
VALUES (2, 2, 'session_999', 'https://stripe.com', 450.00, 'PAID', 'PAYMENT');
