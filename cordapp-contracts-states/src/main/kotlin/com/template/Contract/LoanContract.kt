package com.template.Contract

import com.template.State.LoanState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class LoanContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.Contract.LoanContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands: CommandData{
        class LoanRequest: TypeOnlyCommandData(), Commands
        class LoanApproval: TypeOnlyCommandData(), Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value){
            is Commands.LoanRequest -> verifyLoanRequest(tx, command)
            is Commands.LoanApproval -> verifyLoanApproval(tx, command)
        }
    }

    private fun verifyLoanRequest(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have zero input" using (tx.inputs.isEmpty())
            "Transaction should have one output" using (tx.outputs.size == 1)

            val outputState = tx.outputStates.get(0) as LoanState

            "The loan amount should be positive" using (outputState.amount > 0)
            "The loan status should be null" using (outputState.loanStatus == null)
            "loan application should be signed by the Finance Agency" using (command.signers.contains(outputState.financeAgency.owningKey))
        }
    }

    private fun verifyLoanApproval(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have one input" using (tx.inputs.size == 1)
            "Transaction should have one output" using (tx.outputs.size == 1)

            val inputState = tx.inputStates.get(0) as LoanState
            val outputState = tx.outputStates.get(0) as LoanState

            "The loan amount in input and output should be same" using (inputState.amount == outputState.amount)
            "The credit Rating in input and output should be same" using (inputState.cibilRating == outputState.cibilRating)

            "loan application should be signed by the Bank and Finance Agency" using (command.signers.containsAll(listOf(outputState.bank.owningKey,outputState.financeAgency.owningKey)))
        }
    }

}