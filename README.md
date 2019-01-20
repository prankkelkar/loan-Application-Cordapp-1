<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Loan Application Cordapp - Kotlin

## Instructions for setting up

1. `git clone https://github.com/vardan10/loan-Application-Cordapp.git`
2. `cd loan-Application-Cordapp`
3. `./gradlew deployNodes` - building may take upto a minute (it's much quicker if you already have the Corda binaries).
4. `./build/nodes/runnodes`

At this point you will have a notary node running as well as three other nodes. l. One for the notary and two for each of the three nodes. The nodes take about 20-30 seconds to finish booting up.There should be 4 console windows in total.

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