package com.template

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class EligibilityContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.EligibilityContract"
    }

    // Used to indicate the transaction's intent.
    interface Commands: CommandData{
        class CheckEligibility: TypeOnlyCommandData(),Commands
        class EligibilityApproval: TypeOnlyCommandData(),Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.CheckEligibility -> verifyCheckEligibility(tx, command)
            is Commands.EligibilityApproval -> verifyEligibilityApproval(tx, command)
        }
    }

    private fun verifyCheckEligibility(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have zero input" using (tx.inputs.isEmpty())
            "Transaction should have one output" using (tx.outputs.size == 1)

            val outputState = tx.outputStates.get(0) as EligibilityState

            "CIBIL rating should be null" using (outputState.cibilRating == null)
            "loan application should be signed by the Bank" using (command.signers.contains(outputState.bank.owningKey))
        }
    }

    private fun verifyEligibilityApproval(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have one input" using (tx.inputs.size == 1)
            "Transaction should have one output" using (tx.outputs.size == 1)

            val inputState = tx.inputStates.get(0) as EligibilityState
            val outputState = tx.outputStates.get(0) as EligibilityState

            "The name in input and output should be same" using (inputState.name == outputState.name)
            "CIBIL rating should not be null" using (outputState.cibilRating != null)
            "loan application should be signed by the Credit Rating Agency" using (command.signers.contains(outputState.creditRatingAgency.owningKey))
        }
    }

}