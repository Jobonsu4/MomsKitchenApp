-- =========================================================
-- Mom's Kitchen — Seed Data (MySQL 8 / utf8mb4)
-- Place at: backend/src/main/resources/data.sql
-- =========================================================
SET NAMES utf8mb4;

-- ---------- CORE MENU ----------
INSERT INTO menu (id, name, description, is_active) VALUES
(1, 'Main Menu', 'Our staple Ghanaian dishes', 1);

-- ---------- CATEGORIES ----------
INSERT INTO menu_category (id, menu_id, name, description, display_order, is_active) VALUES
(1, 1, 'Plates', 'Full plates with sides', 1, 1),
(2, 1, 'Sides',  'Perfect add-ons',       2, 1),
(3, 1, 'Drinks', 'House-made beverages',   3, 1);

-- ---------- ITEMS ----------
-- Plates
INSERT INTO menu_item (id, category_id, name, description, price, is_available, image_url, display_order) VALUES
(1, 1, 'Jollof Rice Plate', 'Smoky tomato rice with fried chicken', 12.99, 1, '/img/jollof.jpg',    1),
(2, 1, 'Waakye Plate',      'Rice & beans with gari, shito, salad',     11.99, 1, '/img/waakye.jpg',    2),
(3, 1, 'Fufu with Soup',    'Cassava & plantain dumplings, light soup', 14.99, 1, '/img/fufu and soup.jpg', 3);

-- Sides
INSERT INTO menu_item (id, category_id, name, description, price, is_available, image_url, display_order) VALUES
(4, 2, 'Fried Plantains',   'Crispy & sweet',                            4.50,  1, '/img/plantains.jpg', 1),
(5, 2, 'Extra Shito',       'House spicy pepper sauce',                  1.00,  1, '/img/shito.jpg', 2);

-- Drinks
INSERT INTO menu_item (id, category_id, name, description, price, is_available, image_url, display_order) VALUES
(6, 3, 'Sobolo (Hibiscus)', 'Chilled hibiscus ginger drink',             3.50,  1, '/img/sobolo.jpg',    1);

-- ---------- ADD-ONS ----------
INSERT INTO addon (id, name, description, price_delta, is_active) VALUES
(1, 'Extra Protein', 'Add more chicken/beef', 3.00, 1),
(2, 'Spicy Shito',   'Hot pepper sauce',      0.50, 1),
(3, 'No Onions',     'Hold the onions',       0.00, 1),
(4, 'Meat Upgrade',  'Swap to lamb (+$5)',    5.00, 1);

-- ---------- ITEM ↔ ADD-ON MAPPING ----------
-- Plates allow protein/shito/no-onions; Jollof also allows Goat Upgrade
INSERT INTO menu_item_addon (menu_item_id, addon_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),  -- Jollof
(2, 1), (2, 2), (2, 3),          -- Waakye
(3, 1), (3, 2), (3, 3), (3, 4),  -- Fufu
(4, 2),                          -- Fried Plantains can add shito
(5, 2);                          -- Extra Shito item still lists shito add-on (optional)

-- ---------- PICKUP SLOTS (Fri/Sat/Sun) ----------
-- day_of_week: 0=Sun ... 6=Sat
INSERT INTO pickup_slot (id, day_of_week, start_time, end_time, is_active) VALUES
(1, 5, '16:00:00', '19:00:00', 1),  -- Friday 4–7 PM
(2, 6, '12:00:00', '15:00:00', 1),  -- Saturday 12–3 PM
(3, 0, '12:00:00', '15:00:00', 1);  -- Sunday 12–3 PM
