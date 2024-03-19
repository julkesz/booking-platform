# Booking Platform

A distributed platform for booking tickets across multiple train companies written in Java Spring Boot.

## Description

Customers browse the trains that are available across different train companies and select one or more seats for their itinerary, which are stored in-memory as a quote. Once they have finalized their selection, they confirm their selected seats and receive a ticket for each one. All these tickets combined constitute one single booking that potentially contains tickets from multiple train companies. Meanwhile, managers for each train company or for the platform as a whole can retrieve aggregate statistics on trains and customers.

- The booking platform contacts external train companies to get a list of trains and seats. **REST requests** are made to retrieve the data and to confirm/cancel bookings.

- Users of the booking platform have to authenticate themselves before they can make reservations. **Spring Security** is used to integrate security on the server. The web application authenticates the user by contacting the **Firebase Authentication** server. Once authenticated in the browser, the web application attaches an **OAuth Identity token** containing the user’s identity and attributes to every request sent to the /api/ endpoint. Requests that require authentication are intercepted by the security filter, which can verify the token and make its properties available to the request.

- To better utilize the scalability opportunities that would be available in a cloud environment, indirect communication is used for decoupling background processes. Computations related to creating a new booking is moved to a background worker by using **Cloud Pub/Sub push subscriptions**.

- **ACID properties** are satisfied: users cannot book already reserved seat and they cannot confirm their quotes partially. For example, to satisfy atomicity either all quotes are successfully reserved, or none of them are reserved at all. If the application crashes during operation, it will try complete the booking after recovering. 

- All the bookings and users' information are stored in a **Cloud Firestore database**.

## Getting started
### Dependencies
- Java 17 SDK
- Maven
- Firebase CLI

Firebase CLI can be installed by following instructuions found on [Firebase CLI reference](https://firebase.google.com/docs/cli). If you choose
the standalone binary option, it is recommended to put this executable in the project folder (next to the firebase.json file). When you run this standalone binary, a new terminal will open where you can run the firebase commands.

### Running the application locally
1. Open Firebase CLI and type:
```
firebase emulators:start --project booking-platform
```
2. Once the emulators are running, you can start the application using Maven:
```
mvn spring-boot:run
```
3. Open http://localhost:8080/ to use the application.
