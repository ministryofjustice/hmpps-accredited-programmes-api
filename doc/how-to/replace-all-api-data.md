# Replace all API data

There are two scenarios in which replacing API data may be useful or necessary.

1. Local development and testing with PostgreSQL database and HMPPS Auth instances running in Docker.
2. Initial testing, release and maintenance of the Accredited Programmes service.

The API provides three endpoints that accept bulk data uploads in CSV format.
These endpoints may be used to completely replace the course, prerequisite and offering data served by the API.

The endpoints have been designed to provide a simple way for data prepared in a spreadsheet to be
uploaded to the API. This is a quick fix approach for the initial release of the API. It may also be used to maintain
the data in production until `Courses`, `Prerequisites` or `Offerings` are referenced by other data or applications
using the
UUID 'id' values assigned to these entities by the API.

**N.B.** These ID values will change each time the API data is replaced even if the data itself does not change.
If the IDs of entities are expected to be immutable then these endpoints **must not be used** and should be removed
from the application.

## CSV data formats

The endpoints accept data representing Courses, Prerequisites and Offerings.
Each of these kinds of data may be conveniently downloaded from a spreadsheet to file in the comma-separated values
(CSV) format. For the remainder of this document, including examples, we will assume these three files are named
Courses.csv, Prerequisites.csv and Offerings.csv. In the Kubernetes context, these filenames are required.

The first row in each file is the header row. The values in this row are column names and are recognised by the
endpoints. The header row column names must all be present, even if they are not used, and they must be exactly as shown
below.

Each subsequent row contains the data for one course, offering or prerequisite.

The tables below show the header row column names and the details of the data contained in each column.

### Courses.csv

Contains basic information about each course.

| header value  | data                                                                                                                                     |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| name          | The display name for the course                                                                                                          |
| identifier    | A unique identifier for the course. Rows in the Prerequisites.csv and Offerings.csv files will refer to the course using this identifier |
| description   | The course description. Free text                                                                                                        |
| audience      | One or more 'audience' values. The set of values should be enclosed in double quotes and separated by commas. Example: `"x, y, z"`       |
| alternateName | A short human readable 'tag' for the course. Example: `BNM+`                                                                             |
| comments      | Not used                                                                                                                                 |

### Prerequisites.csv

Contains information about the prerequisites for the courses in Courses.csv. A prerequisite is linked to a course using
the value in the 'identifier' column.

| header value | data                                                                                                                    |
|--------------|-------------------------------------------------------------------------------------------------------------------------|
| name         | The display name for the Prerequisite. Example: `Gender`                                                                |
| course       | Not used                                                                                                                |
| identifier   | One or more course identifiers enclosed in double quotes and separated by commas. Example: `"BNM-SO, BNM-IPVO, BNM-VO"` |
| description  | The prerequisite description. Example: `Male` or `Female`                                                               |
| comments     | Neither used nor stored                                                                                                 |

### Offerings.csv

Contains information about the offerings for the courses in Courses.csv. An offering is linked to a course using the
value in the 'identifier' column.

| header value            | data                                                                                            |
|-------------------------|-------------------------------------------------------------------------------------------------|
| course                  | Not used. The name of the course to which this offering applies                                 |
| identifier              | The course identifier. Must be one of the identifier values from Courses.csv. Example: `BNM-SO` |
| prisonId                | The prison ID for the prison associated with this offering. Examples: MDI, BXI                  |
| organisation            | Not used. A human readable name for the prison                                                  |
| contact email           | The primary contact email address for this offering                                             |
| secondary contact email | An optional secondary contact email address for this offering                                   |                                                    

## Replace all data in a local API instance

This approach assumes that the API instance is using a local HMPPS Auth instance running in 'dev' mode.  
Usually this would be achieved using the docker-compose.yml configuration contained in the root of this project.

### Prerequisites

* Docker running locally. For Macs this is probably Docker Desktop or Rancher Desktop
* Bash 4.2 or later
* jq (https://jqlang.github.io/jq/)

### Procedure

Start a local instance of hmpps-auth and postgresql in Docker compose:

```shell
docker compose up hmpps-auth postgresql -d
```

Start the application. Either using the Gradle `bootRunLocal` command or in an IDE such as IntelliJ.

Pipe the course data (Courses.csv) to `script/local-scripts/put-courses`. This removes all data from the API and
uploads the basic course data.

```shell
cat Courses.csv | script/local-scripts/put-courses
```

Pipe the prerequisite data (Prerequisites.csv) to `script/local-scripts/put-prereq`. This uploads the
data.

```shell
cat Prerequisites.csv | script/local-scripts/put-prereq
```

Pipe the offering data (Offerings.csv) to `script/local-scripts/put-offerings`. This uploads the
data.

```shell
cat Offerings.csv | script/local-scripts/put-offerings
```

## Replace all data in a Kubernetes API service

The API is deployed to three kubernetes namespaces in the Ministry of Justice’s Cloud Platform:  
`hmpps-accredited-programmes-dev`, `hmpps-accredited-programmes-preprod` and
`hmpps-accredited-programmes-prod`. These correspond to the three environments `dev`, `preprod` and `prod`.

### Prerequisites

To replace the data served by the API on one of these environments you must have:

* the Kubernetes command-line client `kubectl` installed
* credentials and permissions to access the namespaces above using `kubectl`
* bash 4.2 or higher
* the `jq` command-line json processor (https://jqlang.github.io/jq/)

On a Mac these tools may be installed using Homebrew: https://brew.sh/

### Procedure

1. Put the three CSV files `Courses.csv`, `Offerings.csv` and `Prerequisites.csv` together in one directory
2. Run script `script/kubernetes-scripts/upload-csv` from that directory. The script takes one argument '-ns <env>'
   where `<env>` is one of `dev`, `preprod` or `prod`

For example, to replace the data served by the 'dev' instance of the API:

```shell
script/kubernetes-scripts/upload-csvs -ns dev
```

There is a companion script `script/kubernetes-scripts/get-courses` that queries the `GET /courses` end-point and
outputs the result to stdout. This may be useful for checking the current state of the API data.

```shell
script/kubernetes-scripts/get-courses -ns dev
```

You can pipe the output of this command into `jq` to process the JSON output. For example, to
list each course in the prod environment by name and ID:

```shell
script/kubernetes-scripts/get-courses -ns prod | jq -c '.[]|{name, id}'
```
