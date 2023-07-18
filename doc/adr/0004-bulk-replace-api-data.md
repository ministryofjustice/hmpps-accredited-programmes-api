# 4. Bulk replace API data

Date: 2023-07-18

## Status

Accepted

## Context

Programme, programme offering and programme prerequisite data for the initial launch of the Accredited Programmes
Service are being prepared in a Google Sheets spreadsheet. We need a simple, repeatable means of uploading this data
into the API. Most spreadsheet applications, including Google Sheets, provide a way for the user to download
copies of a spreadsheet's 'sheets' or 'tabs' in various formats. One of the most widely supported and simple formats
is known as [comma-separated values](https://en.wikipedia.org/wiki/Comma-separated_values) or CSV.

Therefore, a convenient approach to uploading the spreadsheet data to the API would be:

1. Export each tab of the spreadsheet to a separate file in CSV format
2. Provide a mechanism to upload each CSV file to the API. The outcome of the uploads would be that the data currently
   stored in the API would be replaced by that held in the CSV files

How to implement the second step? Three approaches come to mind.

1.
    1. Add three end-points to the API, `PUT /courses`, `PUT /courses/offerings` and `PUT /courses/prerequisites`. Each
       end-point accepts a body in media format `text/csv` parses and processes the supplied data and inserts records
       into the API database
    2. Provide stand-alone scripts, CLI or UI programs that will authenticate with HMPPS Auth, obtain a suitable token
       and send the CSV files to the correct end-points
2.
    1. Provide PUT, POST and DELETE end-points as required
       for `/courses`, `/courses/{courseId}`, `/courses/{courseId}/offerings`,
       `/courses/{courseId}/offerings/{offeringId}`, `/courses/{courseId}/prerequisites` and
       `/courses/{courseId}/prerequisites/{prerequisiteId}`. The PUT and POST methods accept JSON documents
       representing the corresponding entities to be added or updated. The DELETE methods have no need of data beyond
       the relevant IDs
    2. Provide stand-alone scripts, CLI programs or UI programs that will take CSV files downloaded from the
       spreadsheet, parse them and use the end-points created in step 1 above to mutate the API data to align with the
       data in the CSV files
3. Write scripts (Kotlin) to massage the data in the CSV files into a set of database DML statements that can be applied
   to the database

The first approach is considerably simpler than the second and will take less time to implement. In its simplest form
the logic behind the API end-points would delete all existing data and then insert the new data. This is quick and easy
to do, but is unusable in the long term because the API links courses with prerequisites and offerings using UUID
values (ID) that are created when the entity records are inserted. These IDs are the primary keys of the entities and
should not change. Replacing the entities also replaces the IDs which is probably unacceptable when other data such as
referrals links to course data using the IDs.

The third approach, mutating CSV files into DML scripts (INSERT, UPDATE etc) might be even simpler than the first,
especially if the generated DML scripts were added to the API's GitHub repository. The scripts would be applied to the
database using the automated Flyway database migrations that are run each time the API application is started.
Unfortunately the GitHub repository is public, and programme data should not be published on the internet and must not
be stored in the GitHub repository.
Therefore, the DML scripts must be applied directly to the database using a database client such as `psql` or
the PostgreSQL database support in IntelliJ.

Taking this approach requires:

* tunneling a database connection through the MOJ platform to an RDS database using the kubectl command-line client
* extracting database credentials from a Kubernetes secret to authenticate the database connection

Interacting with production databases in this way is generally considered high risk and should be avoided. Therefore,
approach 1 is less risky than approach 3 and is preferred.

At this stage it is unclear how the API data will be managed in the long term. Perhaps the behaviour of the bulk upload
end-points of option 1 above could be refined to mutate existing data, thereby preserving the auto-generated
identities (surrogate keys) of entities. It is more likely that some or all of the end-points enumerated in option 2
above would form the basis of a long-term method of managing course data. How those end-points would be invoked
is unclear at this stage. (UI program? Command-line scripts?)

## Decision

Implement approach 1 above.

## Consequences

Option 1 is not a long-term solution. At this stage a long-term approach to maintaining courses, offerings and
prerequisites is unclear.

### Questions for the long-term solution

* Who will manage the data?
* How often will courses, offerings and prerequisites be added, modified, retired or updated?
* How much time and effort can be invested in this solution?

