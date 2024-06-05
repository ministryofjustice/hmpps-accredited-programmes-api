Refresh the information in our person cache (see [0007-store-names-sentence-dates-and-prison-names-in-our-database.md](..%2Fadr%2F0007-store-names-sentence-dates-and-prison-names-in-our-database.md))

Sometimes it will be useful to refresh the information in our person cache - for example if there is new data added to the table, 
and we want the pre-existing people to also have this data. 

There is an endpoint to do this:

`/person/updateAll`

It will loop through all the referrals in the database and for each one run the updatePerson code (this is the same code that the domain event 
listener uses) 

It is advisable to only do this outside of business hours to limit disruption to either ACP or downstream systems due to the influx of traffic.

