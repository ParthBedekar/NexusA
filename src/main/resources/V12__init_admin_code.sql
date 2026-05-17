CREATE TABLE IF NOT EXISTS admin_codes (
    code_id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO admin_codes (code_id, code, email, used, created_at)
SELECT '00000000-0000-0000-0000-000000000912'::uuid, 'vit-admin-001', 'siddhant.belkhede24@vit.edu', false, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM admin_codes WHERE email = 'siddhant.belkhede24@vit.edu');
