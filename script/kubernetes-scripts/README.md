# Scripts to use with the HMPPS Kubernetes cluster
This directory contains scripts that interact with the API instances deployed to the HMPPS Kubernetes cluster.

Every script takes one argument, `-ns <env>` ,that defines the namespace that it is to use.  `<env>` must be one of
'dev', 'preprod' or 'prod'

## The scripts and their function:
- `get-token` Acquires a token from the hmpps-auth service for the named environment. Printed to stdout.
- `upload-csv` Uploads CSV format files 'Course.csv', 'Offering.csv' and 'CoursePrerequiste.csv' to the named 
   environment.  The script should be invoked from the directory containing the files
- `get-courses` Invoke the `GET /courses` end-point and output the response to stdout
- `start-db-portforward-pod` Starts a pod in the named environment that forwards the Postgresql port for the API 
   instance to the pod. 

Regarding port forwarding for the database. Once a port forwarding pod has been started for an environment
the pod port can be forwarded on to the user using a kubectl command like.
```
kubectl port-forward db-port-forward-pod --namespace=hmpps-accredited-programmes-prod 5432:5432
```
Change the namespace and local port number (the left-hand one of the pair) as needed.