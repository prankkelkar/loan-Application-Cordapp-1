package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class verifyCheckEligibilityFlow(val linearIdentifier: UniqueIdentifier,
                                 val creditRatingAgency : Party):FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()


        // Build the transaction
        // 1. Query loan state by linearId for input state
        val vaultQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(linearIdentifier))
        val inputState = serviceHub.vaultService.queryBy<LoanState>(vaultQueryCriteria).states.first()

        // Create the output state
        val outputState = inputState.state.data.copy(creditRatingAgency = creditRatingAgency)

        // Building the transaction
        val transactionBuilder = TransactionBuilder(notary).
                addInputState(inputState).
                addOutputState(outputState, LoanContract.ID).
                addCommand(LoanContract.Commands.CheckEligibility(), ourIdentity.owningKey)

        // Verify transaction Builder
        transactionBuilder.verify(serviceHub)

        // Sign the transaction
        val signedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        // Notarize and commit
        return subFlow(FinalityFlow(signedTransaction))
    }
}