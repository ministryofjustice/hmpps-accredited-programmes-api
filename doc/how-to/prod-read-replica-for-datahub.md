# Action plan: Production read replica for Datahub ingestion (APG-2659)

Goal: create a read replica of the Production RDS (Postgres) so HMPPS Data Hub
can ingest via AWS DMS (CDC) without affecting the operational database.

Most infrastructure changes are made in the
[Cloud Platform Environments](https://github.com/ministryofjustice/cloud-platform-environments)
repo (our namespace `rds.tf`/`variables.tf`), **not** this repo. Look at another
DPS team's namespace for a worked example. Follow the Datahub guide
"Configure DPS RDS Service for Datahub Ingestion | Read Replica" for the latest
instructions.

## Who does what

| Step | Owner | Action |
|------|-------|--------|
| 0 | Us | Prepare environment / create read replica |
| 1 | Us | Setup connectivity |
| 2 | Us | Configure parameter groups |
| 3 | Us | Setup Postgres user, share credentials |
| 4 | Datahub | Extract contracts |
| 5 | Datahub | Configure logical replication (manual slot on replica) |
| 6 | Datahub | Configure DMS source endpoint |

## Prerequisites

- [ ] Data Assessment completed with Datahub.
- [ ] Confirm Prod Postgres engine version is **>= 16.x** (Datahub supports 16, 17, 18). Upgrade first if not.
- [ ] Decide which environments get the steps (replica in Prod/Pre-Prod; primary-only connectivity/params in Dev/Pre-Prod to test first).
- [ ] Agree a reboot window for Prod (parameter changes need a reboot).

## Tasks (cloud-platform-environments PRs)

### 0. Create read replica (Prod)
- [ ] Add a `read_replica` module using `cloud-platform-terraform-rds-instance` (pin `ref`) with `replicate_source_db` pointing at the primary RDS.
- [ ] Match engine version and instance class appropriately.

### 1. Connectivity
- [ ] Add variable `mp_dps_sg_name` (default `cloudplatform-mp-dps-sg`).
- [ ] Add `data "aws_security_group" "mp_dps_sg"`.
- [ ] Set `vpc_security_group_ids = [data.aws_security_group.mp_dps_sg.id]` on the primary **and** the read replica (primary only in envs without a replica).

### 2. Parameter groups (primary and replica)
- [ ] `rds.logical_replication = 1` (pending-reboot)
- [ ] `shared_preload_libraries = pglogical` (pending-reboot)
- [ ] `max_wal_size = 1024` (immediate)
- [ ] `wal_sender_timeout = 0` (immediate)
- [ ] `max_slot_wal_keep_size = 40000` (immediate)
- [ ] Replica only: `hot_standby_feedback = 1` (immediate) - required so the replica doesn't invalidate DMS replication slots.
- [ ] Reboot instances (primary, then replica) and verify parameters applied.

> Check existing `shared_preload_libraries` value on the module so we don't overwrite required libraries.

### 3. Postgres user (run on primary; replicates to replica)
- [ ] `CREATE ROLE digital_prison_reporting WITH LOGIN PASSWORD '<pw>';` (no `;`, `+`, `%` in the password)
- [ ] `GRANT rds_superuser TO digital_prison_reporting;`
- [ ] `GRANT rds_replication TO digital_prison_reporting;`
- [ ] Verify via `pg_roles` / `pg_auth_members` query (both grants present).
- [ ] Share credentials with Datahub per "Password Management for Datahub Onboarding" (never commit them).

## Datahub-owned steps (coordinate, don't do)
- [ ] 4. Contract extraction (can use `dpr-dps-data-contract-schema-generation`); via DPR jump server using the new user.
- [ ] 5. Replica slot: Datahub creates `dpr_replication_slot` (`test_decoding`) on the replica **just before** starting the DMS task (otherwise it may be invalidated, `wal_status = lost`). `create extension pglogical;` on the primary.
- [ ] 6. DMS source endpoint using the replica host with extra attributes `PluginName=test_decoding;slotName=dpr_replication_slot`.

## Verification
- [ ] Replica reachable from Datahub (port 5432 via SG).
- [ ] Replica lag is healthy; `hot_standby_feedback` on.
- [ ] No negative impact on primary (WAL/disk, `max_slot_wal_keep_size`); add/confirm RDS alerts (`rdsAlertsDatabases` in `helm_deploy/values-prod.yaml`).
- [ ] App continues to use the primary only (no config change in this repo expected).

## Risks
- Reboot causes brief Prod downtime - schedule and announce.
- Replication slots retain WAL; monitor disk usage.
- Superuser-level grant for the ingestion user - confirm with Datahub it's required.
- Replica adds cost.

## Open questions
- Which environments get a replica (Prod only vs Pre-Prod too)?
- Current Prod engine version?
- Who holds the DB admin access to run the user grants?
