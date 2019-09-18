# RESTFUL APT : TRANSACTION Between ACCOUNTS

#### Create Account

```
curl -X POST \
  http://localhost:8080/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{
	"accountNumber":"123451",
	"name":"Pallavi Singh",
	"balance":10
}'
```

#### Get Account Details

```
curl -X GET \
  http://localhost:8080/v1/accounts/123452 \
  -H 'Postman-Token: e0264999-b990-42ba-9f6f-033357544ca3' \
  -H 'cache-control: no-cache'
```

#### Transfer

```
curl -X POST \
  http://localhost:8080/v1/transactions \
  -H 'Content-Type: application/json' \
  -d '{
	"transactionId":"3",
    "sourceAccountNumber":"123451",
    "targetAccountNumber":"123452",
    "amount":3
}'
```