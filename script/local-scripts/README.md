This directory contains scripts that interact with the application when it is run locally against the hmpps-auth and 
postgresql containers managed by the docker-compose.yml file in the root directory of this project.

The application should be run with the 'local','dev' and 'seed' spring profiles enabled.

The scripts are:
* all-courses - invokes the `GET /courses` end-point and outputs the response
* offerings-for-course - takes one argument, a course id, and outputs the response from the `GET /courses/{courseId}/offerings` end-point
* put-courses - Expects a CSV file in the spreadsheet Courses format on stdin and invokes the `PUT /courses` end-point with this data. N.B. invoking this end-point deletes all data from the api before inserting the new data.
* put-offerings - Expects a CSV file in the spreadsheet CourseOfferings format on stdin and invokes the `PUT /courses/offerings` end-point with this data.
* put-prereq - Expects a CSV file in the spreadsheet Prerequisites format on stdin and invokes the `PUT /courses/preprequisites` end-point with this data.
* all-courses-csv - Retrieves the current set of courses in CSV format. The response can be fed to `put-courses`. eg `./all-courses-csv | ./put-courses`
* all-offerings-csv - Retrieves the current set of offerings in CSV format. The response can be fed to `put-offerings`. eg `./all-offerings-csv | ./put-offerings`
* all-prereq-csv - Retrieves the current set of prerequisites in CSV format. The response can be fed to `put-prereq`. eg `./all-prereq-csv | ./put-prereq`

The final script `get-token` is used by the other scripts. It retrieves a valid token from the hmpps-auth instance 
running in docker and prints it to stdout.

The `all-courses-csv`, `all-offerings-csv` and `all-prereq-csv` scripts provide a convenient way
to update course, offering and prerequisite information in the application. To do so
download the CSV data to files,  edit in a spreadsheet program and ensure the changes are save
in CSV format. Upload the modified file(s) using the corresponding `put-courses`, `put-offerings` or `put-prereq`
scripts.  Courses variants and Offerings are soft-deleted when absent from the uploaded CSV.
Returning a Course variant or Offering to the CSV will restore the soft-deleted course/offering in the application.

Courses are distinguished by the value in the `identifier` column. All other values may be changed
and will be applied to the course having that identifier.

Offerings are distinguished by course identifier and prisonId.  All other values may be changed and will
be applied to the offering having that identifier and prisonId.

Prerequisites are hard-deleted.
This is not a problem because in prerequisites do not have unique UUIDs that must be preserved in the application.