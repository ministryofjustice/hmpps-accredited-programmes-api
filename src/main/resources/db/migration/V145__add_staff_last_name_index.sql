-- Add non-UNIQUE index on staff.last_name.
--
-- The SAR (Subject Access Request) custody report renders the staff
-- collection sorted alphabetically by surname so a vettor sees the
-- list in a natural read order (both in the JSON payload and in the
-- rendered PDF). StaffRepository.findByPrisonNumber uses
-- `ORDER BY s.lastName, s.staffId` to guarantee determinism (and to
-- render a sensible order downstream); this index backs the sort so
-- we're not scanning the full staff table for every SAR request.
--
-- Non-UNIQUE: multiple staff routinely share a surname. The tie-break
-- on staff_id (already indexed via idx_staff_staff_id, see V144)
-- guarantees deterministic ordering when surnames collide.
--
-- Cheap, additive, IF NOT EXISTS so re-runnable. No data change.

CREATE INDEX IF NOT EXISTS idx_staff_last_name ON staff(last_name);

