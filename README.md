###### DB challenge

###### Initial Setup
1. Clone the project 
2. Run the application as Spring Boot application
3. Application will start on your localhost
4. Use postman to hit the end point
###### List of endpoints
1. http://localhost/v1/accounts  to create account
request body 
  {
   "accountId":"12",
   "balance":20
   }
2. http://localhost/v1/transaction/transfer/fromAccountId/toAccountId/amount
example http://localhost:18080/v1/transaction/transfer/12/13/10

###### Usage
1. Create two accounts with the balance by using the first endpoint
2. Do transfer


