# Manage infrastructure

The service is hosted on the [MoJ Cloud
Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#getting-started).
It's a platform where we can host our applications and interact with them
through Kubernetes. This requires our applications to all be Dockerised.

Each repository will have a `helm_deploy` directory that specifies configuration
for this service in each of the environments.

Each environment will correspond to a Cloud Platform 'namespace'. The namespace
is an isolated cluster. We can use the [Cloud Platform Environments
repository](https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk)
to define our backing services, certificates etc.

When this app deploys via merging to `main`, CircleCI will automatically
propagate those changes to the cluster. This happens through the
`hmpps/deploy_env` job that is provided by the [MoJ CircleCI
Orb](https://github.com/ministryofjustice/hmpps-circleci-orb).


## Prerequisites

- Be a part of the Ministry of Justice GitHub organisation. This should be done
  as part of your your team onboarding process.
- Be a part of the `accredited-programmes-team` team. This should be done as
  part of your team onboarding process.
- [Follow the Cloud Platform guidance to connect to the Kubernetes
  cluster](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#connecting-to-the-cloud-platform-39-s-kubernetes-cluster).

## Kubernetes cheat sheet

To use Kubernetes to interact with the cluster there's [a cheat
sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/). Keep reading
for tasks we commonly use.

## Common Kubernetes tasks

### View the application logs of a pod

Find the name of the pod you'd like to get the logs for:

```bash
kubectl --namespace hmpps-accredited-programmes-<env> get pods
```

Follow the logs:

```bash
kubectl -n hmpps-accredited-programmes-<env> logs --follow <pod name> --all-containers
```

### Debug a deployment

Sometimes the above steps will be enough to debug any failed deployments. If
not, it can also be helpful to view the deployment logs.

Find the name of the deployment you'd like to get logs for:

```bash
kubectl -n hmpps-accredited-programmes-<env> get deployments
```

View details of the deployment:

```bash
kubectl -n hmpps-accredited-programmes-<env> describe deployment <deployment name>
```

### View/change the value of an environment variable

Environment variables are themselves [defined with
Helm](https://github.com/ministryofjustice/hmpps-accredited-programmes-api/blob/main/helm_deploy/hmpps-accredited-programmes-api/values.yaml#L18).

For environment variables that aren't secrets we can set these values in our
[Helm
charts](https://github.com/ministryofjustice/hmpps-accredited-programmes-api/blob/main/helm_deploy/values-prod.yaml#9).

For environment variables that contain secrets, we can't set these in GitHub so
we have to set the values by hand.

First find the secret set you'd like to view/change:

```bash
kubectl --namespace hmpps-accredited-programmes-<env> get secrets
```

Add the secret name and view or make the change:

```bash
kubectl --namespace hmpps-accredited-programmes-<env> edit secret <secret set name>
```

Consider a [rolling restart](#rolling-restart) to apply this change.

### Rolling restart

Restart an individual service without downtime. Each service will have multiple
containers running. This process will attempt to start a new replica set
alongside the existing set that's currently serving real requests. If the new
set is healthy, Kubernetes will gracefully replace the existing set and then
remove the old. Useful as one way to refresh environment variables.

First find the service name you'd like to restart:

```bash
kubectl --namespace hmpps-accredited-programmes-<env> get services
```

Start the restart:

```bash
kubectl --namespace hmpps-accredited-programmes-<env> rollout restart deployment <service name>
```

You can observe the progress if you like:

```bash
watch kubectl --namespace hmpps-accredited-programmes-<env> get pods
```
