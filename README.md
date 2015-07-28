# foodtruck-api
## Synopsis

This foodtruck-api is used to get nearby food trucks or search exact trucks using location/address/status/objectid. 
foodtruck-api gets source data via Socrata Open Data API.

## Requirements
  Install sbt, java
  
## Start Up API server

 run    ./activator run      in repo root directory

## API Reference

The api can be split into two functions: <br />
  1. get nearby trucks for one user <br />
   you must set the user's latitude and longitude. You can also set one parameter called "keyword", and the api can
   return the results including the keyword. There are other parameters you can set, they are:<br />
   limit (default: 10): the number of return results <br />
   range (default: 1000): the range of distance near the user<br />
   example:<br />
   http://localhost:9000/nearByFoodTruck?latitude=37.7901490874965&longitude=-122.398658184594&keyword=chicken <br />
   
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
