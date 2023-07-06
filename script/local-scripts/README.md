This directory contains scripts that interact with the application when it is run locally against the hmpps-auth and 
postgresql containers managed by the docker-compose.yml file in the root directory of this project.

The application should be run with the 'local','dev' and 'seed' spring profiles enabled.

The scripts are:
* all-courses - invokes the `GET /courses` end-point and outputs the response
* offerings-for-course - takes one argument, a course id, and outputs the response from the `GET /courses/{courseId}/offerings` end-point
* put-courses - Expects a CSV file in the spreadsheet Courses format on stdin and invokes the `PUT /courses` end-point with this data. N.B. invoking this end-point deletes all data from the api before inserting the new data.
* put-offerings - Expects a CSV file in the spreadsheet CourseOfferings format on stdin and invokes the `PUT /courses/offerings` end-point with this data.
* put-prereq - Expects a CSV file in the spreadsheet Prerequisites format on stdin and invokes the `PUT /courses/preprequisites` end-point with this data.

The final script `get-token` is used by the other scripts. It retrieves a valid token from the hmpps-auth instance 
running in docker and prints it to stdout.