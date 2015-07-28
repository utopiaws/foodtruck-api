# foodtruck-api
## Synopsis

This foodtruck-api is used to get nearby food trucks or search exact trucks using location/address/status/objectid. 
foodtruck-api gets source data via Socrata Open Data API.

## Requirements
  Install sbt, java
  
## Code structure
  Core code:
   app/controllers : write the code to take actions when new api requests come. 
   app/filters: include access loggers and error handle filters
   
## Start Up API server

 in repo root directory, command: ./activator run      

## API Reference

The api can be split into two functions: <br />
  1. get nearby trucks for one user <br />
   you must set the user's latitude and longitude. You can also set one parameter called "keyword", and the api can
   return the results including the keyword. There are other parameters you can set, they are:<br />
   limit (default: 10): the number of return results <br />
   range (default: 1000): the range of distance near the user<br />
   example:<br />
   http://localhost:9000/nearByFoodTruck?latitude=37.7901490874965&longitude=-122.398658184594&keyword=chicken <br />
   
   results:  <br />
[
{
location: {
needs_recoding: false,
longitude: "-122.398658184594",
latitude: "37.7901490874965"
},
status: "APPROVED",
expirationdate: "2016-03-15T00:00:00",
permit: "15MFF-0073",
block: "3708",
received: "Mar 16 2015 12:33PM",
facilitytype: "Truck",
blocklot: "3708055",
locationdescription: "01ST ST: STEVENSON ST to JESSIE ST (21 - 56)",
cnn: "101000",
priorpermit: "0",
approved: "2015-06-19T16:12:38",
schedule: "http://bsm.sfdpw.org/PermitsTracker/reports/report.aspx?title=schedule&report=rptSchedule&params=permit=15MFF-0073&ExportPDF=1&Filename=15MFF-0073_schedule.pdf",
address: "50 01ST ST",
applicant: "Scotch Bonnet",
lot: "055",
fooditems: "Jerk chicken: curry chicken: curry goat: curry dhal: Burritos: Fish: Ox tails: rice: beans: veggies.",
longitude: "-122.398658184604",
latitude: "37.7901490737255",
objectid: "632078",
y: "2115738.283",
x: "6013063.33"
}
]
   
   PS: the return results from path nearByFoodTruck satisfy that the status of truck is "APPROVED" and expirationdate        is larger than the request date.
  2. search by location/address/objectid/object <br />
   This function you should input the exact value of the parameter you want to search <br />
   examples:<br />
   http://localhost:9000/searchByObjectid?objectid=632078 <br />
   http://localhost:9000/searchByStatus?status=APPROVED  <br />
   http://localhost:9000/searchByAddress?address=50%2001ST%20ST <br />
   http://data.sfgov.org/resource/rqzj-sfat.json?longitude=-122.398658184594&latitude=37.7901490874965 <br />

   All the results from above api requests satisfy expirationdate is larger than the request date. 

## Tests

sbt test
