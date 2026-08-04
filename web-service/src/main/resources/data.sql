-- Runs once at startup (after Hibernate creates the schema from the @Entity
-- classes, per ddl-auto=create-drop) to seed the H2 database. Order matters:
-- parent rows (dataset) must exist before child rows that reference them via
-- foreign key (generator_config, data_point, comparison).

-- Datasets
INSERT INTO dataset (id, name, description, created_at) VALUES (1, 'Dice Rolls', 'Simulated six-sided dice rolls', '2026-08-01T10:00:00');
INSERT INTO dataset (id, name, description, created_at) VALUES (2, 'Sensor Noise', 'Random noise samples around a baseline', '2026-08-02T09:30:00');

-- Generator configs (one-to-one with dataset)
INSERT INTO generator_config (id, min_value, max_value, sample_count, seed, dataset_id) VALUES (1, 1.0, 6.0, 5, 42, 1);
INSERT INTO generator_config (id, min_value, max_value, sample_count, seed, dataset_id) VALUES (2, -5.0, 5.0, 5, 7, 2);

-- Data points (many-to-one with dataset)
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (1, 3.2, 0, '2026-08-01T10:00:01', 1);
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (2, 5.8, 1, '2026-08-01T10:00:01', 1);
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (3, 1.4, 2, '2026-08-01T10:00:01', 1);
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (4, 4.1, 3, '2026-08-01T10:00:01', 1);
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (5, 2.9, 4, '2026-08-01T10:00:01', 1);
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (6, -1.3, 0, '2026-08-02T09:30:01', 2);
INSERT INTO data_point (id, value, position, generated_at, dataset_id) VALUES (7, 2.6, 1, '2026-08-02T09:30:01', 2);

-- Comparisons (many-to-one with data_point, twice)
INSERT INTO comparison (id, point_a_id, point_b_id, result, difference, compared_at) VALUES (1, 1, 2, 'B_GREATER', 2.6, '2026-08-01T10:05:00');
INSERT INTO comparison (id, point_a_id, point_b_id, result, difference, compared_at) VALUES (2, 6, 7, 'B_GREATER', 3.9, '2026-08-02T09:35:00');
