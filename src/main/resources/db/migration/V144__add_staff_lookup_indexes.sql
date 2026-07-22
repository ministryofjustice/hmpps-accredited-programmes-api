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
-- These indexes are intentionally non-UNIQUE: production data is known to
-- contain multiple staff rows sharing the same `username` and/or `staff_id`. 
-- The surname-projection queries reflect this
-- by returning List<String>; StaffLookupService picks the first result.
-- Promoting these to UNIQUE would require a data audit + dedupe pass.

CREATE INDEX IF NOT EXISTS idx_staff_username ON staff(username);
CREATE INDEX IF NOT EXISTS idx_staff_staff_id ON staff(staff_id);
