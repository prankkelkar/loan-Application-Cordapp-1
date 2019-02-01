package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.Contract.EligibilityContract
import com.template.State.EligibilityState
import com.template.service.Oracle
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.function.Predicate

@InitiatingFlow
@StartableByRPC
class verifyEligibilityApprovalFlow(val eligibilityID: UniqueIdentifier
                                    ):FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Build the transaction
        // 1. Query loan state by linearId for input state
        val vaultQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(eligibilityID))
        val inputState = serviceHub.vaultService.queryBy<EligibilityState>(vaultQueryCriteria).states.first()


        //get cibil rating from oracle
        val panCardNo=inputState.state.data.panCardNo

        val oracleName = CordaX500Name("Oracle", "New York","US")
        val oracle = serviceHub.networkMapCache.getNodeByLegalName(oracleName)?.legalIdentities?.first()
                ?: throw IllegalArgumentException("Requested oracle $oracleName not found on network.")
        val cibilRating = subFlow(QueryCreditRatingFlow(oracle,panCardNo))


        // Create the output state
        val outputState = inputState.state.data.copy(cibilRating = cibilRating)

        // Building the transaction
        val transactionBuilder = TransactionBuilder(notary).
                addInputState(inputState).
                addOutputState(outputState, EligibilityContract.ID).
                addCommand(EligibilityContract.Commands.EligibilityApproval(), ourIdentity.owningKey).
                addCommand(EligibilityContract.Commands.GenerateRating(panCardNo, cibilRating), listOf(oracle.owningKey, ourIdentity.owningKey))

        // Verify transaction Builder
        transactionBuilder.verify(serviceHub)

        //Sign initial transaction
        val ptx = serviceHub.signInitialTransaction(transactionBuilder)


        // For privacy reasons, we only want to expose to the oracle any commands of type `Prime.Create`
        // that require its signature.
        val ftx = ptx.buildFilteredTransaction(Predicate {
            when (it) {
                is Command<*> -> oracle.owningKey in it.signers && it.value is EligibilityContract.Commands.GenerateRating
                else -> false
            }
        })

        val oracleSignature = subFlow(SignCreditRatingFlow(oracle, ftx))
        val stx = ptx.withAdditionalSignature(oracleSignature)


        // Notarize and commit
        return subFlow(FinalityFlow(stx))
    }
}