-- 知识图谱测试数据

-- 知识实体
INSERT INTO knowledge_entity (name, entity_type, properties, source) VALUES
('Customer', 'EntityType', '{"description": "客户实体类型", "attributes": ["id", "name", "email"]}', 'test'),
('Order', 'EntityType', '{"description": "订单实体类型", "attributes": ["id", "amount", "date"]}', 'test'),
('Product', 'EntityType', '{"description": "产品实体类型", "attributes": ["id", "name", "price"]}', 'test'),
('John Doe', 'Customer', '{"email": "john@example.com", "age": 30, "city": "New York"}', 'test'),
('Jane Smith', 'Customer', '{"email": "jane@example.com", "age": 25, "city": "Los Angeles"}', 'test'),
('ORD-001', 'Order', '{"amount": 150.00, "date": "2024-01-15", "status": "completed"}', 'test'),
('ORD-002', 'Order', '{"amount": 230.50, "date": "2024-01-16", "status": "pending"}', 'test'),
('Laptop', 'Product', '{"price": 999.99, "category": "Electronics", "brand": "TechCorp"}', 'test'),
('Mouse', 'Product', '{"price": 29.99, "category": "Electronics", "brand": "TechCorp"}', 'test');

-- 知识关系
INSERT INTO knowledge_relation (source_id, target_id, relation_type, properties) VALUES
((SELECT id FROM knowledge_entity WHERE name = 'John Doe'), 
 (SELECT id FROM knowledge_entity WHERE name = 'ORD-001'), 
 'PLACED_ORDER', 
 '{"timestamp": "2024-01-15T10:00:00Z"}'),
((SELECT id FROM knowledge_entity WHERE name = 'Jane Smith'), 
 (SELECT id FROM knowledge_entity WHERE name = 'ORD-002'), 
 'PLACED_ORDER', 
 '{"timestamp": "2024-01-16T14:30:00Z"}'),
((SELECT id FROM knowledge_entity WHERE name = 'ORD-001'), 
 (SELECT id FROM knowledge_entity WHERE name = 'Laptop'), 
 'CONTAINS', 
 '{"quantity": 1, "unit_price": 999.99}'),
((SELECT id FROM knowledge_entity WHERE name = 'ORD-001'), 
 (SELECT id FROM knowledge_entity WHERE name = 'Mouse'), 
 'CONTAINS', 
 '{"quantity": 2, "unit_price": 29.99}'),
((SELECT id FROM knowledge_entity WHERE name = 'Laptop'), 
 (SELECT id FROM knowledge_entity WHERE name = 'Product'), 
 'INSTANCE_OF', 
 '{}'),
((SELECT id FROM knowledge_entity WHERE name = 'Mouse'), 
 (SELECT id FROM knowledge_entity WHERE name = 'Product'), 
 'INSTANCE_OF', 
 '{}');
