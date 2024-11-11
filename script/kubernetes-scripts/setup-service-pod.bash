#!/bin/bash
namespace=hmpps-accredited-programmes-preprod

hostname=$(hostname)
# Read any named params
while [ $# -gt 0 ]; do

   if [[ $1 == *"--"* ]]; then
        param="${1/--/}"
        declare $param="$2"
   fi

  shift
done

set -o history -o histexpand
set -e
exit_on_error() {
    exit_code=$1
    last_command=${@:2}
    if [ $exit_code -ne 0 ]; then
        >&2 echo "💥 Last command:"
        >&2 echo "    \"${last_command}\""
        >&2 echo "❌ Failed with exit code ${exit_code}."
        >&2 echo "🟥 Aborting"
        exit $exit_code
    fi
}

debug_pod_name=service-pod-$namespace
echo "service pod name: $debug_pod_name"
service_pod_exists="$(kubectl get pods $debug_pod_name || echo 'NotFound')"


if [[ ! $service_pod_exists =~ 'NotFound' ]]; then
  echo "$debug_pod_name exists signing into shell"
  kubectl exec -it -n $namespace $debug_pod_name -- sh
  exit 0
fi

# Get credentials such as RDS identifiers from namespace secrets
echo "🔑 Getting RDS instance from secrets ..."
secret_json=$(cloud-platform decode-secret -s rds-postgresql-instance-output -n $namespace --skip-version-check)
export RDS_INSTANCE_IDENTIFIER=$(echo "$secret_json" | jq -r .data.rds_instance_address | sed s/[.].*//)

kubectl --namespace=$namespace --request-timeout='120s' run \
    --env "namespace=$namespace" \
    --env "RDS_INSTANCE_IDENTIFIER=$RDS_INSTANCE_IDENTIFIER" \
    -it --rm $debug_pod_name --image=quay.io/hmpps/hmpps-probation-in-court-utils:latest \
    --restart=Never --overrides='{ "spec": { "serviceAccount": "hmpps-accredited-programmes-service-account" } }'


