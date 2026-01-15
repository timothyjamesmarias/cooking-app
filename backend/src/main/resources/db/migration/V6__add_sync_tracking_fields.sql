-- Add sync tracking fields to all entities for conflict detection

-- Add version and last_modified to recipes
ALTER TABLE recipes
ADD COLUMN version INT NOT NULL DEFAULT 1,
ADD COLUMN last_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add version and last_modified to ingredients
ALTER TABLE ingredients
ADD COLUMN version INT NOT NULL DEFAULT 1,
ADD COLUMN last_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add version and last_modified to units
ALTER TABLE units
ADD COLUMN version INT NOT NULL DEFAULT 1,
ADD COLUMN last_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add version and last_modified to quantities
ALTER TABLE quantities
ADD COLUMN version INT NOT NULL DEFAULT 1,
ADD COLUMN last_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Create indexes for performance on last_modified columns (useful for sync queries)
CREATE INDEX idx_recipes_last_modified ON recipes(last_modified);
CREATE INDEX idx_ingredients_last_modified ON ingredients(last_modified);
CREATE INDEX idx_units_last_modified ON units(last_modified);
CREATE INDEX idx_quantities_last_modified ON quantities(last_modified);