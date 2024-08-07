This directory contains scripts that interact with the application when it is run locally against the hmpps-auth and 
postgresql containers managed by the docker-compose.yml file in the root directory of this project.

The application should be run with the 'local','dev' and 'seed' spring profiles enabled.

The scripts are:
* all-courses - invokes the `GET /courses` end-point and outputs the response
* offerings-for-course - takes one argument, a course id, and outputs the response from the `GET /courses/{courseId}/offerings` end-point

The final script `get-token` is used by the other scripts. It retrieves a valid token from the hmpps-auth instance 
running in docker and prints it to stdout.
