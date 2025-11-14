-- Update profile_image_url column to TEXT type to support base64 images
-- Run this SQL in your PostgreSQL database

ALTER TABLE users 
ALTER COLUMN profile_image_url TYPE TEXT;

-- Verify the change
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'profile_image_url';
