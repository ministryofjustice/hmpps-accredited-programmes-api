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

## Stage by stage (what to do next)

Terraform for steps 0-2 is in
[cloud-platform-environments#48567](https://github.com/ministryofjustice/cloud-platform-environments/pull/48567) (Prod)
and [#48606](https://github.com/ministryofjustice/cloud-platform-environments/pull/48606) (Preprod, separate PR as only one namespace per PR is allowed; do Preprod first to test)
(`namespaces/live.cloud-platform.service.justice.gov.uk/<namespace>/resources/rds-postgresql.tf`).

| Stage | Owner | What to do | Status |
|-------|-------|------------|--------|
| A. Terraform PR | Us | Add replica, Datahub SG, parameter groups (steps 0-2). | Done - draft PR open, GitHub checks and `plan-live` pass |
| B. Review plan | Us | Confirm plan: replica created, primary updated in place (no destroy/replace), static params `pending-reboot`, `hot_standby_feedback` on replica only. | Done - plan: 9 add, 2 change, 1 destroy (old unused `-upgrade` parameter group) |
| C. Approval | Cloud Platform team | OPA auto-approve fails the allowlist check for a read replica, so request manual approval in #ask-cloud-platform. Mark PR ready for review. | To do |
| D. Merge and apply | Us / Cloud Platform | Merge PR. Apply creates the replica and attaches the new parameter group to the primary. | To do |
| E. Reboot primary | Us | Primary parameter group moves from `default.postgres16` to the custom group. Reboot in an agreed window so `rds.logical_replication` and `shared_preload_libraries` take effect. Then reboot replica if it shows `pending-reboot`. | To do |
| F. Verify parameters | Us | Check `SHOW rds.logical_replication;`, `SHOW shared_preload_libraries;`, replica `SHOW hot_standby_feedback;`, and that the replica is in sync. | To do |
| G. Postgres user | Us (DB admin) | On the primary: create `digital_prison_reporting`, grant `rds_superuser` and `rds_replication`; verify grants. | To do |
| H1. Request Datahub AWS access | Us | Email/Slack Datahub a single point-of-contact email. Datahub raise a ticket, add us to the `hmpps-digital-prison-reporting` GitHub team and create the Terraform-managed placeholder secrets (Prod placeholder may lag non-Prod). | To do |
| H2. Share credentials | Us | In the Datahub AWS account (eu-west-2, role `modernisation-platform-developer`), edit secret `external/dpr-dps-<service>-source-secrets` (see "Datahub secret format"). Never commit credentials. | To do (after H1) |
| I. Extract contracts | Datahub | Extract data contracts using the new user via the DPR jump server. | Waiting on H |
| J. Logical replication | Datahub | `create extension pglogical;` on the primary. Create `dpr_replication_slot` (`test_decoding`) on the replica just before the DMS task starts. | Waiting on I |
| K. DMS endpoint | Datahub | Configure the source endpoint with the replica host and `PluginName=test_decoding;slotName=dpr_replication_slot`. | Waiting on J |
| L. Monitor | Us | Watch replica lag, WAL/disk usage and RDS alerts after ingestion starts. | After K |

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

## Datahub secret format (step H2)

Plaintext JSON in `external/dpr-dps-<service>-source-secrets`:

```json
{"db_name":"<DB_NAME>","endpoint":"<REPLICA_ENDPOINT>","heartbeat_endpoint":"<PRIMARY_ENDPOINT>","password":"<PASSWORD>","port":"5432","user":"digital_prison_reporting","username":"digital_prison_reporting"}
```

- `endpoint` is the **read replica** host; `heartbeat_endpoint` is the **primary** host (only used with a replica).
- `user` and `username` must both be set (known duplication on Datahub's side).
- Datahub environments are dev, test, pre-prod, prod. With only 3 environments our lowest maps to both Datahub dev and test.
- If no secrets are listed, check the console region is Europe London (eu-west-2).

## Datahub-run detail (for reference)

- Contracts (step 4): from the DPR jump server (SSM session), `psql` as `digital_prison_reporting`, run the Datahub table-extraction and primary-key queries, save the output.
- Slot (step 5, replica): drop any old slot first (`select pg_drop_replication_slot('dpr_replication_slot');`), then `select * from pg_create_logical_replication_slot('dpr_replication_slot', 'test_decoding');` and confirm in `pg_replication_slots`. Create it only just before the DMS task starts (needs DMS >= 3.6.1).
- Extension (step 5, primary): `create extension pglogical;` then confirm in `pg_extension`.
- DMS endpoint (step 6): `CaptureDdls=false`, `HeartbeatEnable=true`, port 5432, `Username=digital_prison_reporting`, server is the replica host, extra attributes `PluginName=test_decoding;slotName=dpr_replication_slot`.

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
- Which environments get a replica? Decision: Prod and Preprod. Dev is skipped unless Datahub asks for it (it would be a primary-only PR: SG + parameters, no replica; `endpoint` and `heartbeat_endpoint` both the primary).
- Current Prod engine version?
- Who holds the DB admin access to run the user grants?
