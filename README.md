# Stock assignment for P company
The purpose of this assessment is to how to design CRUD APIs with partial updates.

### Objective
Create a JVM based backend application using REST. That contains the following  endpoints:
* GET /api/stocks (get a list of stocks)
* POST /api/stocks (create a stock)
* GET /api/stocks/1 (get one stock from the list)
* PATCH /api/stocks/1 (update the price of a single stock)
* DELETE/api/stocks/1 (delete a single stock)
The initial list of stocks should be created on application start-up. Use a database that is most appropriate for this use-case (Hint: Use Docker and provide user instructions).

* The stock object contains at least the following fields:
ID, name (String); ,currentPrice (Amount), lastUpdate (Timestamp).
* Configure the GET /api/stocks endpoint to support request pagination (the number of stocks per page must be configurable).
* Each endpoint must be compliant with the HTTP/1.1 and REST standards.
* Use Spring Boot to build and test this application

### Nice to have (Optional)
* The codebase should have a comprehensive documentation of its API (Ex: OpenAPI).
* The codebase should target Java 11 or higher.
* Implementing the project in Kotlin is a plus.

### Implementation
* Treat this application as a real MVP that should go to production.
* All main use cases must be covered by at least one unit or(and) integration test

## Request flow
1. Request reach to Endpoint
2. Request send to the service
3. Request send to the Repository
4. Response return from Repository
5. Service get the response and map the response to Dto then return it to Endpoint 
6. Response return to client
![request-flow](https://user-images.githubusercontent.com/8404721/197818542-e0e98dc0-748a-47d8-a3bd-1ed9dcc34b39.jpg)

## How to run
```bash
# Clone this repository
$ git clone https://github.com/ma-sharifi/stockp

You can run it from Maven directly using the Spring Boot Maven plugin.
$ ./mvnw spring-boot:run
OR
$ mvn spring-boot:run -Dspring-boot.run.profiles=prod or without profile, it will use with default

# To build the code as a docker image, open a command-line 
# window and execute the following command and build image from Dockerfile:
$ mvn clean package dockerfile:build

# Now we are going to use docker-compose to start the actual image. To start the docker image, run your Docker locally adn stay in the directory containing src and run the following command: 
$ docker-compose -f docker/docker-compose.yml up
$ docker-compose -f docker/docker-compose.yml down
```

## HATEOAS
With HATEOAS, a client interacts with a network application whose application servers provide information dynamically through hypermedia. A REST client needs little to no prior knowledge about how to interact with an application or server beyond a generic understanding of hypermedia.
* Pagination provided by HATEOAS for this project.
It means, You can go for the first, next, previous, and last page by reading the link of them in the response of the findall request.
* For simplicity I provide 3 different HTTP Hearer to client know about pages and the count of the total stocks.
* Note: provided `hateoas.disabled` variable in properties file. You can disable HATEOAS links this way hateoas.disabled: true. 

## ResponseDto
There is a ResponseDto object. This object is our response. This object has a List<T> payload.
Client **MUST** find the body of the response here. Due to simplicity for client, payload is always a list. As a result, client just need process this field.
We know an object is an array with a length of one.

## HTTP Status
* Note: provided different HTTP Header for different situation.
1. If the result of `GET` (stocks/1,/stocks) be success HTTP Status 200 and error_code 0.
2. If the result of `POST` (create) be success, the response will contain HTTP Status 201 and error_code 0 and a Location with the url of the newly created entity in header (HTTP Header-> Location:/api/stocks/1).
3. If something be not normal from the client side a HTTP Status 400 (Bad Request) will be return.
4. If the entity was not found HTTP Status will be 404 (Not Found).
5. If something unhandled occurred on the server-side the HTTP Status would be 500.

## Provided 4 different ways for test the application:
1. [Swagger](http://localhost:8080/swagger-ui/index.html)
2. Run test `mvn test`
3. Postman. There is a Postman file of this project in this path located [here](postman/stockp.postman_collection.json)
4. CLI. Commands provided with [HTTPie](https://httpie.io/).

## Exception
Defined different Exceptions for different situations.
Provided a Global Exception handler to help handle exceptions in an easy way.
In `ResponseDto` there is a field with name `error_code` you can see error code of each request.
Global Exception, put the error_code into `ResponseDto` base the condition.
Provided a `message` to show the text result of request.
If `log debug mode` is activated, you can see the `details` of the error in the response.
The sample response of a result with eror at below:

```json
{
  "message": "#Unique index or primary key violation!",
  "details": "Unique index or primary key violation: \"PUBLIC.UNQ_STO_NAME_INDEX_2 ON PUBLIC.T_STOCK(NAME) VALUES 5\"; SQL statement:\ninsert into t_stock (id, current_price, last_update, name) values (default, ?, ?, ?) [23505-199] ;insert into t_stock (id, current_price, last_update, name) values (default, ?, ?, ?)",
  "timestamp": "2022-10-25T20:05:15.220+00:00",
  "error_code": 4005
}
```
## How we can scale the application?
* Use Redis as a cache putting stocks into it.
* Based on Scale Qube, we can partition data in our database (scale by splitting similar things) to different clusters.
* Put log to ELK for monitoring (Or Prometheus and Grafana) for finding issue before they become serious in production.
* Use a cloud database.
* Deploy application on Azure App Service.
* Add CI/CD pipeline for faster deployment.

## Mapping
I used MapStruct for mapping entity to Dto and vice versa. Mapstruct is compile time not runtime. It helps to have a better speed.

## Assumption
* Simplicity is more important than other things. Tried to have a small code.
* I took for every update|partial update `lastUpdate` needs to be updated.
* I can use `HATEOAS` for pagination
* There is one instance of the application running at the moment. 

## Test Coverage
Provided 29 tests(Unit test, Integration test, End to End test)
* 100% Class
* 90% Method
* 87% Line

## API
I described all APIs here. 

### Stock /api/stocks
1. Users can create(add) stock.
* **POST**`/api/stocks` HTTP Status: 201
  *Note: For post because I put the URL of the created entity in the Location HTTP Header, we can remove the body from the response.
2. Get list of all stocks .
* **GET**`/api/stocks` HTTP Status: 200
* Provided by paging. You can use it this way in this service: ?sort=id,desc&page=0&size=2
* `sort=id,desc` it means sort by id by descending order (use asc for ascending). Id is the field of stock.
* size=2 it means paginate the result such a way there are 2 items per page.
* page=0 just return page 0 not any other pages.
* HATEOAS link provided vof this API. Client can reach to the firs, next, prev, and last page with call the url of these names.
3. Get stock by its `id`.
* **GET**`/api/stocks/{id}` HTTP Status: 200
4. Delete a stock by id
* **DELETE**`/api/stocks/{id}` HTTP Status: 204
5. Update a stock by id
* **PUT**`/api/stocks/{id}` HTTP Status: 200
6. Partial update a stock by id
* **PATCH**`/api/stocks/{id}` HTTP Status: 200

## HTTPie

### 1. POST /api/stocks
Users can create a stock
* **POST**`/api/stocks` HTTP Status: 201
#### Request:
```bash
http POST localhost:8080/api/stocks name="Stock#1" current_price=1 
```
#### Successful response:
The header and Location is enough for this API. We can remove the response body. If client needs, It can get url of the just created entity from `Location` in HTTP Header.
```bash
HTTP/1.1 201
Location: http://localhost:8080/api/stocks/8
```
```json
{
    "error_code": 0,
    "message": "Success",
    "payload": [
        {
            "current_price": 1,
            "id": 8,
            "last_update": "2022-10-25T19:48:07.961069Z",
            "name": "Stock#1"
        }
    ],
    "timestamp": "2022-10-25T19:48:07.962+00:00"
}
```

### 2. GET /api/stocks
Get list of all stocks.
* **GET**`/api/stocks` HTTP Status: 200
* Provided by paging. You can use it this way in this service: ?sort=id,desc&page=0&size=2
#### Request:
```bash
http GET localhost:8080/api/stocks page==0 size==2
```
#### Successful response:
* X-Page-Total: 3, it means with page size of 2, we have 3 pages. 
* X-Total-Count: 5, it means all of our data are 5 stocks.
* X-Page-Current: 0 , we are on the page number 0.
```bash
HTTP/1.1 200
X-Page-Current: 0
X-Page-Total: 3
X-Total-Count: 5
```
Becuase we are on the first page, you can just see the next and last url.
```json
{
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 1,
      "id": 1,
      "last_update": "2022-10-25T19:54:30.293767Z",
      "name": "Stock1"
    },
    {
      "current_price": 2,
      "id": 2,
      "last_update": "2022-10-25T19:54:30.329358Z",
      "name": "Stock2"
    }
  ],
  "timestamp": "2022-10-25T19:56:52.736+00:00",
  "_links": {
    "last": {
      "href": "http://localhost:8080/api/stocks?page=2&size=2"
    },
    "next": {
      "href": "http://localhost:8080/api/stocks?page=1&size=2"
    }
  }
}
```
#### Request:
```bash
http GET localhost:8080/api/stocks page==1 size==2
```
#### Successful response:
* X-Page-Total: 3, it means with page size 0f 2, we have 3 pages.
* X-Total-Count: 5, it means all of our data are 5 stocks.
* X-Page-Current: 0 , we are on the page number 0.
```bash
HTTP/1.1 200
X-Page-Current: 1
X-Page-Total: 3
X-Total-Count: 5
```
Becuase we are on the second page, you can see the next, prev,first and, last url.
```json
{
  "_links": {
    "first": {
      "href": "http://localhost:8080/api/stocks?page=0&size=2"
    },
    "last": {
      "href": "http://localhost:8080/api/stocks?page=2&size=2"
    },
    "next": {
      "href": "http://localhost:8080/api/stocks?page=2&size=2"
    },
    "prev": {
      "href": "http://localhost:8080/api/stocks?page=0&size=2"
    }
  },
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 3,
      "id": 3,
      "last_update": "2022-10-25T19:54:30.331327Z",
      "name": "Stock3"
    },
    {
      "current_price": 4,
      "id": 4,
      "last_update": "2022-10-25T19:54:30.332910Z",
      "name": "Stock4"
    }
  ],
  "timestamp": "2022-10-25T20:00:38.548+00:00"
}

```
### 3. GET /api/stocks/{id}
Get stock by its `id`.
* **GET**`/api/stocks/{id}` HTTP Status: 200
#### Request:
```bash
http GET localhost:8080/api/stocks/1
```
#### Successful response:
```bash
HTTP/1.1 200
```
```json
{
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 1,
      "id": 1,
      "last_update": "2022-10-25T19:54:30.293767Z",
      "name": "Stock1"
    }
  ],
  "timestamp": "2022-10-25T20:06:54.684+00:00"
}
```

### 4. DELETE /api/stocks/{id}
4. Delete a stock by id
* **DELETE**`/api/stocks/{id}` HTTP Status: 204
#### Request:
```bash
http DELETE localhost:8080/api/stocks/1
```
#### Successful response:
```bash
HTTP/1.1 204
```

### 5. PUT /api/stocks
Update a stock by id
* **PUT**`/api/stocks/{id}` HTTP Status: 200

#### Request:
```bash
http PUT localhost:8080/api/stocks/3 id=3 name="Updated Stock" current_price=3
```
#### Successful response:
The header and Location is enough for this API. We can remove the response body. If client needs, It can get url of the just created entity from `Location` in HTTP Header.
```bash
HTTP/1.1 200
```
Before update: 
```json
{
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 3,
      "id": 3,
      "last_update": "2022-10-25T19:54:30.331327Z",
      "name": "Stock3"
    }
  ],
  "timestamp": "2022-10-25T20:11:39.879+00:00"
}
```
After update:
```json
{
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 3,
      "id": 3,
      "last_update": "2022-10-25T19:54:30.331327Z",
      "name": "Updated Stock"
    }
  ],
  "timestamp": "2022-10-25T20:12:53.561+00:00"
}
```


### 6. PATCH /api/stocks
Partial update a stock by id
* **PATCH**`/api/stocks/{id}` HTTP Status: 200

#### Request:
```bash
http PATCH localhost:8080/api/stocks/4 id=4 name="Partial Update Stock4"
```
#### Successful response:
The header and Location is enough for this API. We can remove the response body. If client needs, It can get url of the just created entity from `Location` in HTTP Header.
```bash
HTTP/1.1 200
```
Before partial update:
```json
{
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 4,
      "id": 4,
      "last_update": "2022-10-25T19:54:30.332910Z",
      "name": "Stock4"
    }
  ],
  "timestamp": "2022-10-25T20:16:29.488+00:00"
}
```
After partial update:
```json
{
  "error_code": 0,
  "message": "Success",
  "payload": [
    {
      "current_price": 4,
      "id": 4,
      "last_update": "2022-10-25T19:54:30.332910Z",
      "name": "Partial Update Stock4"
    }
  ],
  "timestamp": "2022-10-25T20:16:51.694+00:00"
}
```

