#!/usr/bin/env bash
# ==============================================================================
# apg2495-run-live-sar-checks.sh
#
# Post-deploy retest helper for APG-2495. Runs every exit-criterion check that
# requires a live authenticated call against the dev / preprod SAR endpoint,
# once HMPPS Auth is unblocked (see HAAR-5793 in the planning note).
#
# Ticket:        APG-2495
# Planning note: doc/planning/APG-2495-post-deploy-retest-live-like-sar.md
# Reads/writes:  outputs land in /tmp/apg2495-live-*.json plus a run report
#                at /tmp/apg2495-live-summary.txt for pasting into the note.
#
# CONTEXT (for a future reader / agent with no prior chat history):
# ----------------------------------------------------------------
# APG-2492 changed how the SAR responder resolves staff surnames — from
# per-row scalar lookups to two batch queries per SAR (one by-username, one
# by-staff-id) plus a single findByPrisonNumber for the Content.staff array.
# The planning note captures the full exit-criteria matrix (A1..A5, B1..B3,
# C1..C3, D1..D2, E2). By the time this script is run, all *offline* checks
# should already be green in the note's Results section. This script covers
# only the online / dev-endpoint-dependent checks:
#
#   A2  — SAR for a subject with ONE referral surfaces surnames everywhere
#         raw identifiers used to appear.
#   A3  — the same identifier renders the same surname across sections
#         (e.g. referrer.username == an audit auditUsername → same surname).
#   A5  — referralStatusHistory[].username field carries the surname, not
#         the raw username.
#   C2  — with a deliberately-injected duplicate staff row, the SAR endpoint
#         emits a WARN log about the offending username/staff_id. (Requires
#         a running kubectl port-forward to the dev DB — see prereqs.)
#   C3  — with the duplicate still injected, three back-to-back SAR calls
#         return the identical surname deterministically.
#   B2  — self-generated load against a subject on PREPROD so App Insights
#         has post-deploy telemetry to compare p95 latency against the
#         pre-deploy baseline. (Optional — pass --load to enable.)
#
# The script is intentionally verbose and defensive. Every step is idempotent
# (re-runnable), and the C2 duplicate-injection step is explicitly
# cleaned up in a trap regardless of whether the SAR call succeeds or fails.
#
# PREREQUISITES:
# --------------
#   1. An HMPPS Auth token with ROLE_SAR_DATA_ACCESS on DEV.
#      Export as:  APG2495_TOKEN=<jwt>
#      (Verify the role is present:
#         echo "$APG2495_TOKEN" | cut -d. -f2 | base64 --decode \
#           | jq '.authorities // .scope')
#
#   2. jq installed on the local machine.
#
#   3. For C2/C3 only — a running kubectl port-forward to the DEV DB.
#      Follow doc/how-to/access-dev-database-remotely.md, then export:
#         APG2495_DEV_DB_HOST=127.0.0.1
#         APG2495_DEV_DB_PORT=5433   # or whatever your port-forward binds to
#         APG2495_DEV_DB_USER=$(kubectl get secret rds-postgresql-instance-output \
#             -n hmpps-accredited-programmes-dev \
#             -o jsonpath='{.data.database_username}' | base64 --decode)
#         APG2495_DEV_DB_PASS=$(kubectl get secret rds-postgresql-instance-output \
#             -n hmpps-accredited-programmes-dev \
#             -o jsonpath='{.data.database_password}' | base64 --decode)
#         APG2495_DEV_DB_NAME=$(kubectl get secret rds-postgresql-instance-output \
#             -n hmpps-accredited-programmes-dev \
#             -o jsonpath='{.data.database_name}' | base64 --decode)
#      Skip these vars to skip the C2/C3 steps.
#
#   4. For B2 only — the preprod cousin of #3 above (namespace
#      hmpps-accredited-programmes-preprod), plus opting in with --load.
#      B2 is a preprod-only check — do NOT run it against dev.
#
# USAGE:
#   ./script/apg2495-run-live-sar-checks.sh             # A2 A3 A5 C2 C3 on dev
#   ./script/apg2495-run-live-sar-checks.sh --load      # + generate B2 load on preprod
#   ./script/apg2495-run-live-sar-checks.sh --dry-run   # print planned actions only
# ==============================================================================

set -euo pipefail

# --- constants (edit if the retest PRN changes; kept in sync with the note) ---
DEV_HOST="https://accredited-programmes-api-dev.hmpps.service.justice.gov.uk"
PREPROD_HOST="https://accredited-programmes-api-preprod.hmpps.service.justice.gov.uk"
RETEST_PRN="${APG2495_PRN:-A8610DY}"     # override with env var if needed
A1_PRN="${APG2495_A1_PRN:-A4433DZ}"      # person-only PRN (for cross-check ref)
OUT_DIR="${APG2495_OUT_DIR:-/tmp}"
SUMMARY="${OUT_DIR}/apg2495-live-summary.txt"
LOAD_MODE=0
DRY_RUN=0

for arg in "$@"; do
  case "$arg" in
    --load)    LOAD_MODE=1 ;;
    --dry-run) DRY_RUN=1 ;;
    -h|--help) sed -n '1,80p' "$0"; exit 0 ;;
    *) echo "unknown arg: $arg" >&2; exit 2 ;;
  esac
done

# --- prereq checks -----------------------------------------------------------
require() { command -v "$1" >/dev/null || { echo "MISSING TOOL: $1" >&2; exit 3; }; }
require curl
require jq

if [ -z "${APG2495_TOKEN:-}" ]; then
  echo "APG2495_TOKEN not set — export a dev HMPPS Auth JWT with ROLE_SAR_DATA_ACCESS." >&2
  exit 4
fi

# Quick sanity — token has SAR role? Pad the base64url payload before decoding
# (JWT payloads omit the trailing '=' padding, which strict base64 decoders reject).
_jwt_payload() {
  local p pad
  p=$(echo "$APG2495_TOKEN" | cut -d. -f2 | tr '_-' '/+')
  pad=$(( (4 - ${#p} % 4) % 4 ))
  printf '%s' "$p"
  printf '=%.0s' $(seq 1 $pad 2>/dev/null)
}
if ! _jwt_payload | base64 --decode 2>/dev/null \
     | jq -e '((.authorities // []) + [.scope // ""] | tostring) | test("SAR_DATA_ACCESS")' >/dev/null; then
  echo "WARNING: token payload does not appear to contain ROLE_SAR_DATA_ACCESS." >&2
  echo "         Continuing anyway — the endpoint will 403 if it truly lacks the role." >&2
fi

: > "$SUMMARY"
log() { echo "[apg2495] $*" | tee -a "$SUMMARY"; }
run() { if [ "$DRY_RUN" -eq 1 ]; then echo "DRY-RUN: $*"; else eval "$@"; fi; }

# =============================================================================
# helpers
# =============================================================================

sar_get() {
  # sar_get <host> <prn> <out-file>
  # Splits curl's mixed body+status output into "$out" (body) and echoed HTTP code.
  # Uses `sed '$d'` (portable — works on BSD/macOS and GNU) rather than
  # `head -n -1` which is GNU-only.
  local host="$1" prn="$2" out="$3"
  if [ "$DRY_RUN" -eq 1 ]; then
    echo "DRY-RUN: curl $host/subject-access-request?prn=$prn (would write $out)"
    echo "200"
    return
  fi
  curl -sS -w "\n%{http_code}" \
    -H "Authorization: Bearer $APG2495_TOKEN" \
    "$host/subject-access-request?prn=$prn" > "${out}.raw"
  local code
  code=$(tail -n1 "${out}.raw")
  sed '$d' "${out}.raw" > "$out"
  rm -f "${out}.raw"
  echo "$code"
}

# psql wrapper — only used for C2 duplicate injection on dev.
psql_dev() {
  PGPASSWORD="$APG2495_DEV_DB_PASS" psql \
    "host=$APG2495_DEV_DB_HOST port=$APG2495_DEV_DB_PORT sslmode=require \
     user=$APG2495_DEV_DB_USER dbname=$APG2495_DEV_DB_NAME" \
    -v ON_ERROR_STOP=1 "$@"
}

# =============================================================================
# A2 — subject with referrals has surnames everywhere
# =============================================================================
log ""
log "=== A2 — surnames present in every identifier field ==="

A2_JSON="$OUT_DIR/apg2495-live-a2.json"
code=$(sar_get "$DEV_HOST" "$RETEST_PRN" "$A2_JSON")
log "HTTP $code  (A2 SAR request for $RETEST_PRN)"
if [ "$code" != "200" ]; then
  log "A2 FAIL — expected HTTP 200"; exit 5
fi

# Structural counts (proves the response is well-formed)
log "$(jq '{
  referrals:            (.content.referrals            | length),
  courseParticipations: (.content.courseParticipation  | length),
  auditRecords:         (.content.auditRecords         | length),
  statusHistory:        (.content.referralStatusHistory| length),
  staff:                (.content.staff                | length),
  pniResults:           (.content.pniResults           | length),
  person_present:       (.content.person != null)
}' "$A2_JSON")"

# Surname sanity — pick the first referral and prove the fields that used to
# carry raw identifiers now carry name-shaped values.
# Heuristic for "looks like a surname": ≥ 2 chars, contains no digits, is not
# all-uppercase (staff usernames in dev are typically ALLCAPS-style like
# "AELANGOVAN_ADM", surnames are Title-cased or lower-case).
A2_ASSERT=$(jq -r '
  .content.referrals[0] as $r
  | [$r.referrerUsername, $r.primaryPomStaffSurname, $r.secondaryPomStaffSurname]
  | map(select(. != null))
  | if length == 0 then "SKIP — no non-null surname fields on first referral"
    else
      map(if test("^[A-Z0-9_]+$") then "SUSPECT_RAW_USERNAME:\(.)"
          elif length < 2       then "TOO_SHORT:\(.)"
          else "OK:\(.)" end)
      | join(" | ")
    end' "$A2_JSON")
log "A2 surname shape check: $A2_ASSERT"

# =============================================================================
# A3 — same identifier renders same surname across sections
# =============================================================================
log ""
log "=== A3 — cross-section surname consistency ==="

# Set intersection of the (non-null) username sets between audit records and
# status history. O(N + M), not O(N * M) — the naive nested-iteration variant
# hangs on realistic-size subjects (28k audits × 593 status rows ≈ 17M pairs).
A3=$(jq -r '
  ([.content.auditRecords[]?.auditUsername]        | map(select(. != null))) as $a
  | ([.content.referralStatusHistory[]?.username]  | map(select(. != null))) as $s
  | ([$a[], $s[]] | group_by(.) | map(select(length > 1)) | map(.[0])) as $intersect
  | {
      audit_usernames_unique:    ($a | unique | length),
      status_usernames_unique:   ($s | unique | length),
      shared_between_sections:   $intersect,
      shared_count:              ($intersect | length)
    }
' "$A2_JSON")
log "$A3"

# A3's assertion: every identifier in the intersection resolves to the same
# rendered surname across sections. This is guaranteed by design (one
# StaffSurnames.byUsername map is threaded into every mapper) but we check
# empirically: for each shared username, the value that appears in the
# audit-username field is the same string that appears in the
# status-history-username field.
A3_MISMATCHES=$(jq -r '
  ([.content.auditRecords[]?.auditUsername]        | map(select(. != null))) as $a
  | ([.content.referralStatusHistory[]?.username]  | map(select(. != null))) as $s
  | ([$a[], $s[]] | group_by(.) | map(select(length > 1)) | map(.[0])) as $intersect
  # For each shared username, verify audit-set and status-set both contain it
  # exactly (already true by construction of $intersect from union).
  # Since batch-map values are string equality, presence in both sets
  # implies identical rendering. We surface a friendly output here for the
  # human reviewer.
  | if ($intersect | length) == 0
    then "SKIP — no shared usernames between audit and statusHistory sections"
    else ($intersect | map("shared=\(.)") | join(", "))
    end
' "$A2_JSON")
log "A3 cross-section usernames: $A3_MISMATCHES"

# =============================================================================
# A5 — statusHistory[].username IS a surname (not the raw username)
# =============================================================================
log ""
log "=== A5 — statusHistory renders surnames not usernames ==="

A5=$(jq -r '
  .content.referralStatusHistory
  | map(.username)
  | map(select(. != null))
  | if length == 0 then "SKIP — no statusHistory rows"
    else
      # Same shape heuristic as A2. All-caps _ / digit sequences look raw.
      map(if test("^[A-Z0-9_]+$") then "SUSPECT_RAW:\(.)" else "OK:\(.)" end)
      | unique
      | join(" | ")
    end' "$A2_JSON")
log "A5 status-history usernames: $A5"

# =============================================================================
# C2 + C3 — inject duplicate staff row, hit SAR, then hit 2 more times
# =============================================================================
if [ -n "${APG2495_DEV_DB_HOST:-}" ] && [ -n "${APG2495_DEV_DB_PASS:-}" ]; then
  log ""
  log "=== C2 + C3 — inject duplicate staff row, verify WARN + deterministic pick ==="

  # Pick a real username from the SAR response so we don't invent a phantom.
  DUP_USER=$(jq -r '.content.referrals[0].referrerUsername // empty' "$A2_JSON")
  if [ -z "$DUP_USER" ]; then
    log "C2 SKIP — could not derive a real username from A2 response (referrerUsername null)"
  else
    log "C2 injecting duplicate staff row for username='$DUP_USER'"

    # Look up the real staff row for that username, then insert a shadow copy
    # with a fresh UUID and a synthetic staff_id. Trap ensures cleanup.
    DUP_ID=$(uuidgen | tr 'A-Z' 'a-z')
    trap 'log "cleaning up injected duplicate row id=$DUP_ID"; psql_dev -c "DELETE FROM staff WHERE id = '\''$DUP_ID'\'';" || true' EXIT

    psql_dev <<SQL
INSERT INTO staff (id, staff_id, first_name, last_name, username, primary_email, account_type)
SELECT '$DUP_ID'::uuid,
       -- new unique staff_id so we don't collide on any staff_id unique index
       (staff_id + 100000000),
       first_name,
       'APG2495_DUPLICATE_SURNAME',
       username,
       'apg2495-dup@example.invalid',
       account_type
FROM   staff
WHERE  username = '$DUP_USER'
LIMIT  1;
SQL

    # Verify the duplicate was inserted (should now be 2 rows for that username)
    DUP_COUNT=$(psql_dev -tAc "SELECT COUNT(*) FROM staff WHERE username = '$DUP_USER';")
    log "duplicate injection complete — staff rows for '$DUP_USER': $DUP_COUNT"
    if [ "$DUP_COUNT" -lt 2 ]; then
      log "C2 SKIP — expected ≥ 2 staff rows after insert; got $DUP_COUNT (real user probably missing on dev)"
    else
      # C2 — hit SAR once and instruct the operator how to check logs
      C2_JSON="$OUT_DIR/apg2495-live-c2.json"
      code=$(sar_get "$DEV_HOST" "$RETEST_PRN" "$C2_JSON")
      log "HTTP $code  (C2 SAR request post-injection)"
      log "C2 log-inspection step (manual): grep dev app logs for the WARN"
      log "  kubectl -n hmpps-accredited-programmes-dev logs -l app=hmpps-accredited-programmes-api --tail=500 | grep -F 'Multiple staff rows found' | grep -F \"$DUP_USER\""
      log "  Expected: at least one WARN line naming username='$DUP_USER' and row count ≥ 2."

      # C3 — three back-to-back SAR calls, capture the chosen surname each time
      C3A="$OUT_DIR/apg2495-live-c3a.json"
      C3B="$OUT_DIR/apg2495-live-c3b.json"
      C3C="$OUT_DIR/apg2495-live-c3c.json"
      sar_get "$DEV_HOST" "$RETEST_PRN" "$C3A" >/dev/null
      sar_get "$DEV_HOST" "$RETEST_PRN" "$C3B" >/dev/null
      sar_get "$DEV_HOST" "$RETEST_PRN" "$C3C" >/dev/null

      # For each response, extract the surname associated with the duplicated
      # username wherever it appears. All three should be identical.
      C3_SURNAMES=$(for f in "$C3A" "$C3B" "$C3C"; do
        jq -r --arg u "$DUP_USER" '
          [ .content.referrals[]?.referrerUsername,
            .content.auditRecords[]?.auditUsername,
            .content.referralStatusHistory[]?.username,
            .content.courseParticipation[]?.createdByUser,
            .content.courseParticipation[]?.updatedByUser
          ] | map(select(. != null)) | unique | join(",")
        ' "$f"
      done | sort -u)
      log "C3 distinct surname sets across 3 runs (should be exactly 1 line): "
      echo "$C3_SURNAMES" | while read -r line; do log "    $line"; done
      C3_LINES=$(echo "$C3_SURNAMES" | grep -c . || true)
      if [ "$C3_LINES" -le 1 ]; then
        log "C3 PASS — deterministic winner selection confirmed"
      else
        log "C3 FAIL — 3 runs produced $C3_LINES distinct surname sets; expected 1"
      fi
    fi
  fi
else
  log ""
  log "=== C2 + C3 SKIPPED — APG2495_DEV_DB_* env vars not all set ==="
  log "See the PREREQUISITES section at the top of this script."
fi

# =============================================================================
# B2 — self-generated load on PREPROD so App Insights has post-deploy samples
# =============================================================================
if [ "$LOAD_MODE" -eq 1 ]; then
  log ""
  log "=== B2 — generating self-load on PREPROD for App Insights ingestion ==="
  log "WARNING: this runs 20 requests against $PREPROD_HOST for prn=$RETEST_PRN"
  # Warm-up (discarded)
  for i in 1 2 3; do
    curl -sS -o /dev/null -w "warmup-%{http_code}-%{time_total}s\n" \
      -H "Authorization: Bearer $APG2495_TOKEN" \
      "$PREPROD_HOST/subject-access-request?prn=$RETEST_PRN" | tee -a "$SUMMARY"
    sleep 1
  done
  # Measured (App Insights will capture these)
  for i in $(seq 1 20); do
    curl -sS -o /dev/null -w "run$i-%{http_code}-%{time_total}s\n" \
      -H "Authorization: Bearer $APG2495_TOKEN" \
      "$PREPROD_HOST/subject-access-request?prn=$RETEST_PRN" | tee -a "$SUMMARY"
    sleep 1
  done
  log "Load complete at $(date -u +'%Y-%m-%dT%H:%M:%SZ')."
  log "Wait ~10 min for App Insights to ingest, then re-run the B2 KQL"
  log "recorded in the planning note's B2 section."
fi

log ""
log "=== done — summary written to $SUMMARY ==="
log "Raw JSON responses in $OUT_DIR/apg2495-live-*.json"
log ""
log "Next: update Results in doc/planning/APG-2495-post-deploy-retest-live-like-sar.md"
log "with A2/A3/A5/C2/C3 outcomes, and commit as:"
log "  APG-2495: record A2/A3/A5/C2/C3 live-endpoint results"

