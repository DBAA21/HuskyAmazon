-- =============================================
-- 1. 清理旧数据 (⭐ 已启用: 防止主键冲突并重置ID)
-- =============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 2. 插入分类 (Categories)
-- =============================================
INSERT INTO categories (name, description) VALUES
                                               ('Electronics', 'Gadgets, computers, smartphones, and accessories.'),
                                               ('Books', 'Fiction, non-fiction, technical, and educational books.'),
                                               ('Clothing', 'Men and women fashion, shoes, and accessories.'),
                                               ('Home & Kitchen', 'Appliances, furniture, and decor for your home.'),
                                               ('Sports & Outdoors', 'Fitness equipment, camping gear, and sportswear.');

-- =============================================
-- 3. 插入商品 (Products)
-- =============================================

-- --- Electronics (ID: 1) ---
-- 图片来源: Unsplash (稳定图库)

INSERT INTO products (name, description, price, original_price, stock, image_url, category_id) VALUES
                                                                                                   ('iPhone 15 Pro', 'Titanium design, A17 Pro chip, 48MP Main camera. The most powerful iPhone ever.', 999.00, 1099.00, 50, 'https://images.unsplash.com/photo-1696446701796-da61225697cc?q=80&w=800&auto=format&fit=crop', 1),

                                                                                                   ('MacBook Air M2', 'Supercharged by M2 chip. 13.6-inch Liquid Retina display. Up to 18 hours of battery life.', 1099.00, 1299.00, 20, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca4?q=80&w=800&auto=format&fit=crop', 1),

                                                                                                   ('Sony WH-1000XM5', 'Industry-leading noise canceling headphones with Auto NC Optimizer.', 348.00, 399.00, 35, 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?q=80&w=800&auto=format&fit=crop', 1),

                                                                                                   ('4K Gaming Monitor', '27-inch 144Hz IPS Monitor with 1ms response time and G-SYNC compatibility.', 299.00, 450.00, 15, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?q=80&w=800&auto=format&fit=crop', 1),

                                                                                                   ('Logitech MX Master 3S', 'Performance wireless mouse with ultra-fast scrolling and 8K DPI tracking.', 99.00, NULL, 100, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?q=80&w=800&auto=format&fit=crop', 1),

                                                                                                   ('Canon EOS R50', 'Compact mirrorless camera for content creators. 4K video and 24.2 MP photos.', 679.00, 799.00, 8, 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?q=80&w=800&auto=format&fit=crop', 1);


-- --- Books (ID: 2) ---
-- 图片来源: Open Library (图书专用库)

INSERT INTO products (name, description, price, original_price, stock, image_url, category_id) VALUES
                                                                                                   ('Spring Boot in Action', 'A comprehensive guide to building Spring applications by Craig Walls.', 45.00, 60.00, 50, 'https://covers.openlibrary.org/b/id/10523478-L.jpg', 2),

                                                                                                   ('Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin.', 42.00, 55.00, 30, 'https://covers.openlibrary.org/b/id/8259443-L.jpg', 2),

                                                                                                   ('The Great Gatsby', 'The classic novel of the Jazz Age by F. Scott Fitzgerald.', 10.50, NULL, 200, 'https://covers.openlibrary.org/b/id/8432049-L.jpg', 2),

                                                                                                   ('Atomic Habits', 'An Easy & Proven Way to Build Good Habits & Break Bad Ones.', 14.00, 27.00, 150, 'https://covers.openlibrary.org/b/id/10603777-L.jpg', 2),

                                                                                                   ('Introduction to Algorithms', 'The bible of algorithms. Essential for every computer scientist.', 85.00, 120.00, 10, 'https://covers.openlibrary.org/b/id/8267232-L.jpg', 2);


-- --- Clothing (ID: 3) ---
-- 图片来源: Unsplash

INSERT INTO products (name, description, price, original_price, stock, image_url, category_id) VALUES
                                                                                                   ('Men''s Casual T-Shirt', '100% Cotton, comfortable fit. Available in various colors.', 15.99, 25.00, 100, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=800&auto=format&fit=crop', 3),

                                                                                                   ('Slim Fit Jeans', 'Classic blue denim jeans with a modern slim fit cut.', 49.99, NULL, 60, 'https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?q=80&w=800&auto=format&fit=crop', 3),

                                                                                                   ('Winter Parka Jacket', 'Heavy duty warm winter jacket with faux fur hood.', 129.99, 199.99, 25, 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?q=80&w=800&auto=format&fit=crop', 3),

                                                                                                   ('Running Sneakers', 'Lightweight and breathable running shoes for daily workouts.', 59.99, 89.99, 40, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop', 3);


-- --- Home & Kitchen (ID: 4) ---
-- 图片来源: Unsplash

INSERT INTO products (name, description, price, original_price, stock, image_url, category_id) VALUES
                                                                                                   ('High-Speed Blender', 'Perfect for smoothies, soups, and frozen desserts. 1200W motor.', 89.00, 120.00, 30, 'https://images.unsplash.com/photo-1570222094114-28a9d88a27e6?q=80&w=800&auto=format&fit=crop', 4),

                                                                                                   ('Coffee Maker', 'Programmable 12-cup coffee maker with glass carafe.', 39.99, 49.99, 45, 'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?q=80&w=800&auto=format&fit=crop', 4),

                                                                                                   ('Modern Desk Lamp', 'LED desk lamp with adjustable brightness and color temperature.', 25.00, NULL, 80, 'https://images.unsplash.com/photo-1507473888900-52e1adad5452?q=80&w=800&auto=format&fit=crop', 4),

                                                                                                   ('Air Fryer', 'Healthy frying with little to no oil. 4 Quart capacity.', 65.00, 100.00, 20, 'https://images.unsplash.com/photo-1626146957879-13098d6c783c?q=80&w=800&auto=format&fit=crop', 4);


-- --- Sports & Outdoors (ID: 5) ---
-- 图片来源: Unsplash

INSERT INTO products (name, description, price, original_price, stock, image_url, category_id) VALUES
                                                                                                   ('Yoga Mat', 'Non-slip exercise mat for yoga, pilates, and floor workouts.', 19.99, NULL, 120, 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?q=80&w=800&auto=format&fit=crop', 5),

                                                                                                   ('Adjustable Dumbbells', 'Set of 2 adjustable dumbbells. Weight ranges from 5 to 52.5 lbs.', 199.00, 299.00, 10, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?q=80&w=800&auto=format&fit=crop', 5),

                                                                                                   ('Mountain Bike', '21-speed mountain bike with front suspension and disc brakes.', 350.00, 450.00, 5, 'https://images.unsplash.com/photo-1576435728678-be95f39e8ab6?q=80&w=800&auto=format&fit=crop', 5),

                                                                                                   ('Camping Tent', 'Waterproof 4-person tent for family camping trips.', 110.00, 150.00, 15, 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?q=80&w=800&auto=format&fit=crop', 5),

                                                                                                   ('Tennis Racket', 'Professional grade carbon fiber tennis racket.', 149.00, 220.00, 20, 'https://images.unsplash.com/photo-1617083934555-ac7d4fee8909?q=80&w=800&auto=format&fit=crop', 5);