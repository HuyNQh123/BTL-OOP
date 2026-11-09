INSERT INTO categories (name, type, icon, color, user_id, is_system, is_active, created_at)
SELECT 'Mục khác', 'EXPENSE', 'bi-three-dots', '#607D8B', NULL, TRUE, TRUE, NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Mục khác'
      AND type = 'EXPENSE'
      AND is_system = TRUE
      AND user_id IS NULL
);
