# Access DB remotely

## Get Database secrets:

```bash
kubectl -n hmpps-accredited-programmes-dev get secret rds-postgresql-instance-output -o json | jq '.data | map_values(@base64d)'
````

"database_name": "XXXX",
"database_password": "XXXX",
"database_username": "XXXX",
"rds_instance_address": "XXXX",
"rds_instance_endpoint": "XXXX"

## Create port forwarding pod:

### check if it is already created first:
```bash
kubectl -n hmpps-accredited-programmes-dev get pods
````

### if there is no pod called port-forward-pod then create it:
change the 'host_from_secrets' to the value from the secrets above
```bash
kubectl -n hmpps-accredited-programmes-dev run port-forward-pod --image=ministryofjustice/port-forward --env="REMOTE_HOST=host_from_secrets" --env="REMOTE_PORT=5432" --env="LOCAL_PORT=5432"
````

### set up port forwarding
Port will be 1533 but you can change that to your desired port.
```bash
kubectl -n hmpps-accredited-programmes-dev port-forward port-forward-pod 5433:5432
````

### Now you should be able to log into the database using your favourite database client using the secrets from the first step and localhost:1533 as the host/port