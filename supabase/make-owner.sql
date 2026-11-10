-- ==============================================================================
-- SIFOX STORE — ONE-TIME OWNER PROMOTION SCRIPT
-- Run this in your Supabase SQL Editor after signing up with your owner email.
-- ==============================================================================

UPDATE public.profiles
SET role = 'owner', updated_at = NOW()
WHERE id IN (
    SELECT id FROM auth.users
    WHERE LOWER(email) = LOWER('mdsifatmozumder07@gmail.com')
);

-- Verification Query: Check that the owner role has been assigned
SELECT id, email, full_name, phone, role, updated_at
FROM public.profiles
WHERE role = 'owner';
