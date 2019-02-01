package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.Contract.LoanContract
import com.template.State.EligibilityState
import com.template.State.LoanState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class verifyLoanApprovalFlow(val eligibilityID: UniqueIdentifier, val loanstatus: Boolean):FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // Get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()


        // Build the transaction
        // 1. Query loan state by linearId for input state
        val vaultEligibilityQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(eligibilityID))
        val EligibilityStateData = serviceHub.vaultService.queryBy<EligibilityState>(vaultEligibilityQueryCriteria).states.first().state.data

        // 1. Query loan state by linearId for input state
        val vaultLoanQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(EligibilityStateData.loanId))
        val inputState = serviceHub.vaultService.queryBy<LoanState>(vaultLoanQueryCriteria).states.first()

        // Create the output state
        val outputState = inputState.state.data.copy(loanStatus=loanstatus)

        // Building the transaction
        val transactionBuilder = TransactionBuilder(notary).
                addInputState(inputState).
                addOutputState(outputState, LoanContract.ID).
                addCommand(LoanContract.Commands.LoanApproval(), ourIdentity.owningKey, outputState.financeAgency.owningKey)

        // Verify transaction Builder
        transactionBuilder.verify(serviceHub)

        // Sign the transaction
        val partiallySignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        // Send transaction to the seller node for signing
        val otherPartySession = initiateFlow(outputState.financeAgency)
        val completelySignedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(otherPartySession)))

        // Notarize and commit
        return subFlow(FinalityFlow(completelySignedTransaction))
    }
}

@InitiatedBy(verifyLoanApprovalFlow::class)
class InvoiceSettlementResponderFlow(val otherpartySession: FlowSession): FlowLogic<Unit>(){
    @Suspendable
    override fun call() {
        val flow = object : SignTransactionFlow(otherpartySession){
            override fun checkTransaction(stx: SignedTransaction) {
                // Any sanity checks on this transaction
            }
        }
        subFlow(flow)
    }
}
