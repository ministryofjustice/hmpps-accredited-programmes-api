# NOMIS Prisoner Merged Events

Occasionally prisoner numbers are merged in NOMIS. This can happen for various reasons and results in the prisoner having a different prisoner number to that
which we have stored against a referral. 
This can cause issues with our service as we rely on the prisoner number to identify the correct person and calls to downstream services such as prisoner
searches in NOMIS start to fail as the "old" prisoner number is not found.


### Listening to NOMIS Prisoner Merged Events

NOMIS publishes `prison-offender-events.prisoner.merged`  events when a prisoner number is merged.
 
The service listens to this event event in the [DomainEventsListener](../../src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/listener/DomainEventsListener.kt) 
and updates the prisoner number across the following tables:

- `person`
- `referral`
- `course_participation`
- `pni_result`

_NB. Prisoner number can also be found in the `audit_record` table but is **not** updated there to maintain a correct audit history._


### Fixing historic add hoc prisoner number merges

Existing older referrals can sometimes have incorrect or pre-merge prisoner numbers, as a result of merges occurring in NOMIS prior to the implementation of the event listener detailed above.
Fixing them involves calling a couple of API endpoints in Prod from PostMan.

Accessing these endpoints requires a personal HMPPS Auth production client with the `ACCREDITED_PROGRAMMES_API` role. This can be obtained by requesting from the HAAR team.


### Finding the new prisoner number

The people search is used to search for a person in NOMIS using the `forename` and `surname` held in the `person` DB table. It returns a list of people that match the search criteria and discretion should be used to select the correct person and the new prisoner number when multiple results are returned.

- [POST /people/search](../../src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/restapi/controller/PeopleController.kt)

It can be called with the following JSON body:

```json
{
  "forename": "John",
  "surname": "Smith"
}
```




### Updating the service with the new prisoner number

An admin API endpoint is used to update the service with the new prisoner number, and calls the same underlying code as the event listener.

- [PUT /admin/person/update-prisoner-number](../../src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsaccreditedprogrammesapi/restapi/controller/AdminController.kt)

It can be called with the following JSON body:

```json
{
  "currentPrisonerNumber": "A12456C",
  "newPrisonerNumber": "A67676H"
}
```
