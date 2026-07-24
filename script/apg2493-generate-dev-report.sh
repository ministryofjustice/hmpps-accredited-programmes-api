#!/usr/bin/env bash
# ==============================================================================
# apg2493-generate-dev-report.sh
#
# Fetches a live SAR JSON response from the DEV Accredited Programmes API for
# a given PRN and writes it to /tmp, then prints a short summary focused on
# the APG-2493 enrichment (nested `originalReferral` block on each referral).
#
# Ticket:        APG-2493 (enrich SAR referrals with a nested originalReferral)
# Planning note: doc/planning/APG-2495-post-deploy-retest-live-like-sar.md
#                (§ "APG-2493 dev report" — see bottom of note)
# Prereqs:
#   - APG2495_TOKEN exported (HMPPS Auth JWT with ROLE_SAR_DATA_ACCESS on dev).
#     See the planning note for how to mint one via HAAR-5793 credentials.
#   - jq installed.
#
# Usage:
#   APG2495_TOKEN=<jwt> ./script/apg2493-generate-dev-report.sh                 # defaults PRN=A8610DY
#   APG2495_TOKEN=<jwt> ./script/apg2493-generate-dev-report.sh A1234BC         # override PRN
# ==============================================================================
set -euo pipefail

PRN="${1:-A8610DY}"
BASE_URL="https://accredited-programmes-api-dev.hmpps.service.justice.gov.uk"
OUT="/tmp/apg2493-dev-${PRN}-$(date +%Y%m%d-%H%M%S).json"

if [[ -z "${APG2495_TOKEN:-}" ]]; then
  echo "ERROR: APG2495_TOKEN not set. See doc/planning/APG-2495-post-deploy-retest-live-like-sar.md for how to mint one." >&2
  exit 1
fi

echo "==> GET ${BASE_URL}/subject-access-request?prn=${PRN}"
HTTP=$(curl -sS -o "$OUT" -w '%{http_code}' \
  -H "Authorization: Bearer ${APG2495_TOKEN}" \
  -H 'Accept: application/json' \
  "${BASE_URL}/subject-access-request?prn=${PRN}")

echo "==> HTTP ${HTTP}"
echo "==> saved: ${OUT} ($(wc -c <"$OUT") bytes)"

if [[ "$HTTP" != "200" ]]; then
  echo "---- response body (first 40 lines) ----"
  head -40 "$OUT"
  exit 2
fi

echo
echo "==> deployed dev build:"
curl -sS "${BASE_URL}/info" | jq '{commit: .git.commit.id, version: .build.version, time: .build.time}'

echo
echo "==> top-level section row counts:"
jq '{
  referrals:              (.content.referrals              // [] | length),
  courseParticipation:    (.content.courseParticipation    // [] | length),
  auditRecords:           (.content.auditRecords           // [] | length),
  referralStatusHistory:  (.content.referralStatusHistory  // [] | length),
  pniResults:             (.content.pniResults             // [] | length),
  staff:                  (.content.staff                  // [] | length),
  person:                 (.content.person != null)
}' "$OUT"

echo
echo "==> APG-2493 originalReferral coverage:"
jq '
  .content.referrals as $r
  | {
      total_referrals:                 ($r | length),
      with_originalReferralId:         ($r | map(select(.originalReferralId  != null)) | length),
      with_populated_originalReferral: ($r | map(select(.originalReferral    != null)) | length)
    }' "$OUT"

echo
echo "==> first populated originalReferral (if any):"
jq '[.content.referrals[] | select(.originalReferral != null)][0] // "none — no referral in this subject has an originalReferral"' "$OUT"

echo
echo "==> keys present on the first populated originalReferral:"
jq '[.content.referrals[] | select(.originalReferral != null)][0].originalReferral // {} | keys' "$OUT"

echo
echo "Done. Attach ${OUT} to the OSAR handover (or upload a redacted excerpt if PII is a concern)."

