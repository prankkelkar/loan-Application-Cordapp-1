<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Loan Application Cordapp - Kotlin

## Instructions for setting up

1. `git clone https://github.com/vardan10/loan-Application-Cordapp.git`
2. `cd loan-Application-Cordapp`
3. `./gradlew deployNodes` - building may take upto a minute (it's much quicker if you already have the Corda binaries).
4. `./build/nodes/runnodes`
5. In a new terminal - run partyA API Server
    ```./gradlew runPartyAServer```
6. In a new terminal - run partyB API Server
    ```./gradlew runPartyBServer```
7. In a new terminal - run partyC API Server
    ```./gradlew runPartyCServer```

At this point you will have a notary node running as well as three other nodes. The nodes take about 20-30 seconds to finish booting up.There should be 4 console windows in total. (Plus 3 terminal for API)

## Using the CorDapp via the console:
1. Start the Loan Application
In PartyA Console type:
```
start LoanRequestFlow name: "Vardan", amount: 30000, bank: "PartyB"
```

2. Get Linear Id of Loan Application
InParty B console type:
```
run vaultQuery contractStateType: com.template.LoanState
```

3. Send loan application to credit rating Agency
InParty B console type:
```
start verifyCheckEligibilityFlow loanID: "<LOAN_LINEAR_ID>", creditRatingAgency: "PartyC"
```

4. Get Linear Id of Loan Eligibility
InParty B console type:
```
run vaultQuery contractStateType: com.template.EligibilityState
```

5. Create a CIBIL Rating
InParty C console type:
```
start verifyEligibilityApprovalFlow eligibilityID: "<Eligibility_LINEAR_ID>", cibilRating: 800
```

6. Approve/Reject Loan Application
InParty B console type:
```
start verifyLoanApprovalFlow eligibilityID: "<Eligibility_LINEAR_ID>", loanstatus: true
```

## Using the CorDapp via the Spring Boot APIs:
1. Start the Loan Application
```
curl -X POST \
  http://localhost:8080/loan/LoanRequest \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F name=Vardan \
  -F amount=40000 \
  -F bank=PartyB
```

2. Get Linear Id of Loan Application
```
curl -X GET http://localhost:8080/loan/GetLoans
```

3. Send loan application to credit rating Agency
```
curl -X POST \
  http://localhost:8081/eligibility/CheckEligibility \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F loanID=<LOAN_LINEAR_ID> \
  -F creditRatingAgency=PartyC
```

4. Get Linear Id of Loan Eligibility
```
curl -X GET http://localhost:8080/eligibility/GetEligibilities
```

5. Create a CIBIL Rating
InParty C console type:
```
curl -X POST \
  http://localhost:8082/eligibility/VerifyEligibility \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F eligibilityID=<ELIGIBILITY_LINEAR_ID> \
  -F cibilRating=800
```

6. Approve/Reject Loan Application
InParty B console type:
```
curl -X POST \
  http://localhost:8081/loan/LoanApproval \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F eligibilityID=<ELIGIBILITY_LINEAR_ID> \
  -F loanStatus=true
```
