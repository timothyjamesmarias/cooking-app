-- Add quantity_id column to recipe_ingredients junction table
ALTER TABLE recipe_ingredients
ADD COLUMN quantity_id BIGINT;

-- Add foreign key constraint for quantity_id
ALTER TABLE recipe_ingredients
ADD CONSTRAINT fk_recipe_ingredients_quantity FOREIGN KEY (quantity_id) REFERENCES quantities (id);

-- Index for lookups by quantity (e.g., finding which recipe uses a specific quantity)
CREATE INDEX idx_recipe_ingredients_quantity ON recipe_ingredients(quantity_id);

-- Add local_id columns to existing tables for sync engine
ALTER TABLE recipes
ADD COLUMN local_id VARCHAR(255) UNIQUE;

ALTER TABLE ingredients
ADD COLUMN local_id VARCHAR(255) UNIQUE;

-- Index for sync engine lookups
CREATE INDEX idx_recipes_local_id ON recipes(local_id);
CREATE INDEX idx_ingredients_local_id ON ingredients(local_id);