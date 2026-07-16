-- Add non-UNIQUE indexes on staff.username and staff.staff_id.
--
-- The SAR (Subject Access Request) custody report performs two point-lookups
-- on the staff table per resolved field
-- (StaffRepository.findLastNameByUsername / findLastNameByStaffId). Neither
-- column was previously indexed (only the UUID primary key on `id`). The join
-- in StaffRepository.findByPrisonNumber
-- (s.staff_id = r.primary_pom_staff_id OR s.staff_id = r.secondary_pom_staff_id)
-- also benefits from the staff_id index.
--
-- These indexes are intentionally non-UNIQUE. The runtime code already
-- assumes uniqueness (returns String? / StaffEntity?) and would throw
-- NonUniqueResultException if duplicates existed, but enforcing UNIQUE at
-- the DB level requires a data audit first — tracked as a follow-up.

CREATE INDEX IF NOT EXISTS idx_staff_username ON staff(username);
CREATE INDEX IF NOT EXISTS idx_staff_staff_id ON staff(staff_id);
