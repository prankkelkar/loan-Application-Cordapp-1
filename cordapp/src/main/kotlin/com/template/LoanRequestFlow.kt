package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.Contract.LoanContract
import com.template.State.LoanState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class LoanRequestFlow(val name: String,
                      val amount: Int,
                      val panCardNo: String,
                      val bank: Party):FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Create the output state
        val outputState = LoanState(name, amount, panCardNo, ourIdentity, bank, null, null, UniqueIdentifier())

        // Building the transaction
        val transactionBuilder = TransactionBuilder(notary).
                addOutputState(outputState, LoanContract.ID).
                addCommand(LoanContract.Commands.LoanRequest(), ourIdentity.owningKey)

        // Verify transaction Builder
        transactionBuilder.verify(serviceHub)

        // Sign the transaction
        val signedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        // Notarize and commit
        return subFlow(FinalityFlow(signedTransaction))
    }
}