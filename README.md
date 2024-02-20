# CryptoApp

The application simulates a cryptocurrency exchange with user registration and cryptocurrency trading capabilities.

## Features

1. **Registration with Email Confirmation:**
   Users go through a registration process with email confirmation by receiving a token via email.

2. **FIAT/STABLECOIN Deposits and Withdrawals:**
   Users can simulate depositing funds to the exchange and withdrawing funds from the exchange.

3. **Tracking Real-time Cryptocurrency Prices:**
   Users have access to real-time cryptocurrency prices with automatic updates every 1 minute.

4. **Transaction History:**
   Users can view the history of their transactions.

5. **Browsing Cryptocurrency Pairs and Saving Transactions to PDF:**
   Users can browse available cryptocurrency pairs and save their transactions to a PDF file.

## System Requirements

CryptoApp requires the following system requirements:

1. **Java 11:**
   Ensure that you have Java 11 installed. You can download it from [Oracle's website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

2. **Spring Boot 3.0.5:**
   The application is built on the Spring Boot platform version 3.0.5. Check the [official Spring Boot project page](https://spring.io/projects/spring-boot) for additional information and installation instructions.

3. **PostgreSQL Database:**
   Configure a PostgreSQL database and adjust the `application.properties` file to set up the connection to the database.

## Installation

1. **Cloning the Repository:**
   Clone this repository to your local machine:

   ```bash
   git clone https://github.com/piotr1wojtaszek/CryptoApp.git
   cd CryptoApp

   or download through the browser:
   https://github.com/piotr1wojtaszek/cryptoApp
   
   set the properties for email in application.properties, i.e. host, email, password.

## Interaction via Postman
To test API functionality, use the link below:
https://cloudy-satellite-406118.postman.co/workspace/New-Team-Workspace~8a719b32-4367-4a7d-94d5-24a51ab5e286/folder/15166833-007f0bde-117b-446b-b832-15bfc176149d?ctx=documentation

Note that most requests require using BasicAuth in the Authorization section, providing the user's login and password. Passing data in requests can be done in two ways:
By entering parameters in the Params section.
By passing them in the Body section in JSON format.

## Example of Use:

Run JDE along with the project and the necessary libraries, launch Postman.

**Registration:**

cryptoApp/app/registrationController/register account
Pass user data in the Body:
{
"username": "login123",
"email": "youremail@example.com",
"password": "password"
}

After sending the request, a verification email should be delivered to the specified email. You can also copy the returned token and pass it as a parameter in the "register confirmation token" GET request.
cryptoApp/app/registrationController/register confirmation token
After confirming registration, you can use the account.

**Deposit/Withdrawal of funds:**

cryptoApp/app/tradeController/transfer
Pass data in the Body depending on the Transaction Type; choose the appropriate TradeType:
{
// "tradeType": "WITHDRAW",
"tradeType": "DEPOSIT",
"fiatSymbol": "USD",
"amount": 1000
}

**Cryptocurrency Trading:**

cryptoApp/app/tradeController/trade crypto
Pass data in the Body depending on the transaction type:
{
"tradeType": "BUY",
// "tradeType": "SELL",
"cryptoToTradeSymbol": "ADA",
"baseCryptoSymbol": "USDT",
"amount": 20,
"unitPrice": 0.383
}

**Downloading PDF:**

Before downloading the PDF, go to the project. In the TradeService class, replace the String named DOWNLOAD_AREA with the appropriate address to save the file.
