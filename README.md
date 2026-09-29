"# IGAppStarter"

# For the first routes and if you are starting with the apache camel basics , please refer to the below link and follow

# the playlist to understand the working of routes.

# Also can clone (https://github.com/UtkarshSinghProtecons25/RestAPI_for_apache.git) for sample crud operations that can be consumed

# in initial learning . Please go through the Readme.md in the repo before using it.

https://www.youtube.com/watch?v=-PRbLgnjisI&list=PLxYSIlUk9lvWLKcNVx9sD588H2sG2Grl5

# Apache Camel – Vehicle Scrap Integration

Prerequisites:

Clone the repo(https://github.com/UtkarshSinghProtecons25/RestAPI_for_apache.git) for consuming the apis used in the below scrap api
Install Postgresql since the database being used is postgres but can tweak as per the needs.

## Overview

This project is an **Apache Camel + Spring Boot integration application** that orchestrates multiple REST APIs involved in the vehicle-scrapping workflow.

The application exposes a REST endpoint that accepts a scrap request and coordinates the following operations:

1. User login or signup
2. Retrieve the authenticated user's vehicles
3. Retrieve payment details associated with the vehicle/lender
4. Validate whether the requested action is `scrap`
5. Submit the vehicle scrap request

The integration is implemented using **Apache Camel routes**, processors, exchange properties, REST DSL, and exception handling.

The project also contains additional Camel routes for other integration use cases.

---

## Technologies

- Java
- Spring Boot
- Apache Camel
- Apache Camel Spring Boot
- Apache Camel REST DSL
- Jackson
- JSONPath
- REST APIs
- Maven

---

## Project Structure

```text
src/main/java
│
├── firstRoute
│   │
│   ├── routes
│   │   └── ScrapVehicleRoute.java
│   │
│   ├── processors
│   │   ├── FirstProcess.java
│   │   ├── LoginResponseProcessor.java
│   │   └── VehicleResponseProcessor.java
│   │
│   └── exceptions
│       ├── IgUnauthorizedException.java
│       └── UserNotFoundException.java
│
└── application.properties
```

Additional routes and processors can be added under the corresponding packages as the integration project grows.

---

# Scrap Vehicle Integration

## REST Endpoint

The main REST endpoint exposed by this route is:

```http
POST /scrap
```

The endpoint acts as an orchestration layer between the client and the underlying insurance/vehicle APIs.

---

## Request

A typical request can contain:

```json
{
  "email": "utkarsh@u.com",
  "password": "utkarsh",
  "action": "scrap"
}
```

For a new user, the request can additionally contain signup information:

```json
{
  "email": "utkarsh@u.com",
  "password": "utkarsh",
  "usertype": "new",
  "fullname": "Utkarsh_K",
  "action": "scrap"
}
```

### Important Fields

| Field      | Description                                                        |
| ---------- | ------------------------------------------------------------------ |
| `email`    | User's email address                                               |
| `password` | User's password                                                    |
| `usertype` | Used to determine whether signup or login should be performed      |
| `fullname` | User's name when signup is required                                |
| `action`   | Determines the requested operation. `scrap` triggers the scrap API |

---

# Integration Flow

The overall flow is:

```text
                    POST /scrap
                         |
                         v
               +-------------------+
               |   FirstProcess    |
               +-------------------+
                         |
                         v
                User type present?
                   /          \
                 YES           NO
                  |             |
                  v             v
              Signup API     Login API
                  \             /
                   \           /
                    v         v
              LoginResponseProcessor
                         |
                         v
                  Vehicle API
                         |
                  HTTP 200?
                    /     \
                  YES      NO
                   |        |
                   v        STOP
              Payment API
                   |
             HTTP 200 + action=scrap?
                  /          \
                YES           NO
                 |             |
                 v             |
             Scrap API         |
                 |             |
                 v             v
              Response      Return payment
```

---

# Route Details

## 1. REST Entry Point

```java
rest("/scrap")
        .post()
        .routeId("login-or-signup")
        .to("direct:login_or_signup");
```

This exposes:

```http
POST /scrap
```

and forwards the request to:

```text
direct:login_or_signup
```

---

# 2. Login / Signup Route

Route ID:

```text
call-login-api
```

The route starts by setting the request method and content type:

```java
.setHeader(Exchange.HTTP_METHOD, constant("POST"))
.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
```

The `FirstProcess` processor processes the incoming request.

The route then checks whether `usertype` is present:

```java
.choice()
    .when().jsonpath("$.usertype", true)
```

If `usertype` is present, the request is sent to:

```text
POST /api/v1/auth/signup
```

Otherwise it is sent to:

```text
POST /api/v1/auth/login
```

The configured base URL is read from:

```properties
sampleapi_port=http://localhost:8080
```

Therefore the actual endpoints become:

```text
http://localhost:8080/api/v1/auth/signup
```

and:

```text
http://localhost:8080/api/v1/auth/login
```

---

# 3. Login Response Processing

After the login operation, the:

```java
LoginResponseProcessor
```

processes the response.

The processor is responsible for extracting information from the authentication response, such as the JWT token.

The JWT is stored in an exchange property:

```text
jwtToken
```

This property is subsequently used when calling protected APIs.

The authorization header is created using:

```java
.setHeader(
    "Authorization",
    simple("Bearer ${exchangeProperty.jwtToken}")
)
```

---

# 4. Vehicle API

Route ID:

```text
call-getvehicle
```

The vehicle API is called using:

```http
GET /api/v1/vehicles
```

The JWT token is passed as:

```http
Authorization: Bearer <JWT>
```

The response is then processed by:

```text
VehicleResponseProcessor
```

This processor extracts the information required for the subsequent payment API call.

For example, the lender ID is stored as:

```text
lenderId
```

in the Camel exchange.

---

# 5. Vehicle API Response Handling

The route checks the HTTP response code:

```java
.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
```

If the Vehicle API returns:

```http
200 OK
```

the route continues to:

```text
direct:payment_details_api
```

If the response is not `200`, the route stops processing.

The original Vehicle API response remains available in the Camel exchange.

---

# 6. Payment Details API

Route ID:

```text
get-payment-details
```

The payment API is called using the lender ID stored in the exchange:

```java
.toD(
    "{{sampleapi_port}}/api/v1/payments/${exchangeProperty.lenderId}..."
)
```

For example:

```text
GET /api/v1/payments/123
```

The dynamic endpoint is created using Camel's `toD()`.

---

# 7. Scrap Action Validation

After receiving the Payment API response, the route checks two conditions:

```java
return status != null
        && status == 200
        && "scrap".equals(action);
```

The conditions are:

1. Payment API response must be `200`
2. The original request's `action` exchange property must be `scrap`

Only when both conditions are satisfied is the Scrap API called.

```java
.to("direct:scrap_vehicle_api")
```

This prevents the Scrap API from being called when:

- Payment API fails
- Payment API returns a non-200 response
- The requested action is not `scrap`
- The request does not contain the expected action

If the conditions are not satisfied, the route does not continue to the Scrap API and the existing Payment API response remains available.

---

# 8. Scrap Vehicle API

Route ID:

```text
scrap_vehicle_api
```

The final API call is:

```http
POST /api/v1/scraps
```

The route sets:

```http
Content-Type: application/json
```

and:

```http
Authorization: Bearer <JWT>
```

The body sent to the Scrap API is:

```json
{
  "actionOnVehicle": "scrap"
}
```

The route therefore converts the original request into the required payload for the Scrap API.

---

# Exchange Properties

The integration uses Camel Exchange Properties to maintain information between routes.

Important properties include:

| Property   | Purpose                                                            |
| ---------- | ------------------------------------------------------------------ |
| `jwtToken` | Stores the JWT obtained from authentication                        |
| `lenderId` | Stores the lender ID obtained while processing vehicle information |
| `action`   | Stores the requested action, such as `scrap`                       |

Exchange properties are useful because the message body changes as different REST APIs are called.

For example:

```text
Original Request
      ↓
Login Response
      ↓
Vehicle Response
      ↓
Payment Response
      ↓
Scrap Request
```

Instead of relying on the current message body for information needed later, required values can be stored as exchange properties.

---

# Exception Handling

The route defines custom exception handlers.

## Unauthorized Exception

```java
onException(IgUnauthorizedException.class)
```

The exception is converted into:

```http
401 Unauthorized
```

with a JSON response:

```json
{
  "message": "exception message"
}
```

---

## User Not Found Exception

```java
onException(UserNotFoundException.class)
```

The exception is converted into:

```http
404 Not Found
```

with:

```json
{
  "message": "exception message"
}
```

---

# HTTP Error Handling

The downstream APIs use:

```text
throwExceptionOnFailure=false
```

This is important because Camel does not automatically throw an HTTP exception for responses such as:

```text
400
401
404
500
```

Instead, the route can inspect:

```java
Exchange.HTTP_RESPONSE_CODE
```

and decide how to continue.

For example:

```java
.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
```

checks the downstream API response explicitly.

---

# Configuration

The external API base URL is configured in:

```text
src/main/resources/application.properties
```

Example:

```properties
sampleapi_port=http://localhost:8080
```

The route uses:

```java
{{sampleapi_port}}
```

instead of hard-coding the URL.

For example:

```java
.to("{{sampleapi_port}}/api/v1/auth/login")
```

This allows the base URL to be changed without modifying the Java route.

---

# Running the Application

## Prerequisites

Make sure the following are installed:

- Java
- Maven
- Spring Boot
- Apache Camel dependencies
- The dependent Sample API application

The Sample API should be running on the configured port.

Example:

```text
http://localhost:8080
```

---

## Start the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or build the project:

```bash
mvn clean package
```

Then run the generated JAR:

```bash
java -jar target/<application-name>.jar
```

---

# Testing the Scrap Endpoint

Using Postman or another REST client:

```http
POST http://localhost:<camel-port>/scrap
Content-Type: application/json
```

Request:

```json
{
  "email": "utkarsh@u.com",
  "password": "utkarsh",
  "action": "scrap"
}
```

For a new user:

```json
{
  "email": "utkarsh@u.com",
  "password": "utkarsh",
  "usertype": "new",
  "fullname": "Utkarsh_K",
  "action": "scrap"
}
```

---

# Route IDs

The current implementation contains the following main route IDs:

| Route ID              | Purpose                      |
| --------------------- | ---------------------------- |
| `login-or-signup`     | REST entry point             |
| `call-login-api`      | Login/signup processing      |
| `call-getvehicle`     | Retrieve vehicle details     |
| `get-payment-details` | Retrieve payment details     |
| `scrap_vehicle_api`   | Submit vehicle scrap request |

---

# Processors

## FirstProcess

Responsible for processing the initial `/scrap` request.

Typical responsibilities include:

- Reading the incoming JSON
- Extracting request information
- Storing required values in Exchange Properties
- Preparing the request for authentication

---

## LoginResponseProcessor

Responsible for processing the authentication response.

Typical responsibilities include:

- Reading the login response
- Extracting the JWT
- Storing the JWT as an Exchange Property

Example property:

```text
jwtToken
```

---

## VehicleResponseProcessor

Responsible for processing the vehicle API response.

Typical responsibilities include:

- Reading the vehicle response
- Extracting vehicle/lender information
- Storing the required information in Exchange Properties

Example:

```text
lenderId
```

---

# Design Approach

The project follows an **integration/orchestration pattern** where Camel acts as the intermediary between multiple REST services.

Instead of exposing all downstream APIs directly to the client, Camel coordinates the workflow:

```text
Client
  |
  v
Camel
  |
  +---- Authentication API
  |
  +---- Vehicle API
  |
  +---- Payment API
  |
  +---- Scrap API
```

This allows business flow and integration logic to remain in the Camel layer while individual services remain responsible for their respective operations.

---

# Additional Camel Routes

This project contains additional Camel routes besides the vehicle-scrapping flow.

The project uses concepts including:

- REST DSL
- `RouteBuilder`
- Direct endpoints
- Processors
- Exchange Properties
- Dynamic endpoints using `toD()`
- HTTP endpoints
- JSONPath
- Choice/When/Otherwise
- Exception handling
- HTTP headers
- JWT propagation
- Request/response transformation

The additional routes follow similar Apache Camel integration patterns and were developed as part of the project's broader Camel learning and implementation work.

A useful reference for the Apache Camel learning material used during development is:

[Apache Camel learning playlist](https://www.youtube.com/watch?v=-PRbLgnjisI&list=PLxYSIlUk9lvWLKcNVx9sD588H2sG2Grl5&utm_source=chatgpt.com)

---

# Key Camel Concepts Used

### REST DSL

```java
rest("/scrap").post()
```

Used to expose REST APIs from Camel.

### Direct Endpoint

```java
.to("direct:payment_details_api")
```

Used to connect Camel routes internally.

### Dynamic Endpoint

```java
.toD("{{sampleapi_port}}/api/v1/payments/${exchangeProperty.lenderId}")
```

Used when part of the endpoint URL is determined dynamically.

### Exchange Properties

```java
exchange.setProperty("lenderId", lenderId);
```

Used to maintain data throughout the Camel exchange.

### Choice

```java
.choice()
    .when(...)
        .to(...)
.end()
```

Used for conditional routing.

### Processor

```java
.process(new FirstProcess())
```

Used for custom Java processing and transformation.

### Exception Handling

```java
onException(SomeException.class)
```

Used to convert Java exceptions into appropriate REST responses.

---

# Summary

The `/scrap` integration provides a multi-step orchestration flow:

```text
POST /scrap
      |
      v
Login / Signup
      |
      v
Get Vehicles
      |
      v
Get Payment Details
      |
      |-- Payment != 200 --> Return response
      |
      |-- Payment == 200
              |
              |-- action != scrap --> Do not call Scrap API
              |
              |-- action == scrap
                        |
                        v
                  Scrap Vehicle
```

The design keeps authentication data, vehicle information, and requested actions in Camel Exchange Properties where necessary, while allowing the message body to represent the response/request of the current downstream API.

This makes the Camel layer responsible for **routing, orchestration, conditional processing, authentication propagation, and API integration**, while the underlying Spring Boot APIs remain responsible for their respective business operations.
