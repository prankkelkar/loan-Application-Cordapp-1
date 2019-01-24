package net.corda.server.controllers

import com.template.State.EligibilityState
import com.template.verifyCheckEligibilityFlow
import com.template.verifyEligibilityApprovalFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import net.corda.server.NodeRPCConnection
import net.corda.server.models.EligibilitySimpleObj
import net.corda.server.models.toSimpleObj
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Define CorDapp-specific endpoints in a controller such as this.
 */
@RestController
@RequestMapping("/eligibility") // The paths for GET and POST requests are relative to this base path.
class EligibilityController(private val rpc: NodeRPCConnection) {

    private val rpcOps = rpc.proxy

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @PostMapping(value = "/CheckEligibility", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    private fun CheckEligibility(@RequestParam(value = "loanID") loanID: String,
                                 @RequestParam(value = "creditRatingAgency") creditRatingAgency: String): ResponseEntity<String> {

        // 1. Get party objects for the counterparty.
        val linearID = UniqueIdentifier.fromString(loanID)
        val creditRatingAgencyIdentity = rpcOps.partiesFromName(creditRatingAgency, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for $creditRatingAgency.")

        // 2. Start the LoanRequest flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    verifyCheckEligibilityFlow::class.java,
                    linearID,
                    creditRatingAgencyIdentity
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // 3. Return the result.
        return ResponseEntity.status(status).body(message)
    }

    @PostMapping(value = "/VerifyEligibility", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    private fun verifyEligibility(@RequestParam(value = "eligibilityID") eligibilityID: String,
                                  @RequestParam(value = "cibilRating") cibilRating: Int): ResponseEntity<String> {

        // 1. Get party objects for the counterparty.
        val linearID = UniqueIdentifier.fromString(eligibilityID)

        // 2. Start the LoanRequest flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    verifyEligibilityApprovalFlow::class.java,
                    linearID,
                    cibilRating
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // 3. Return the result.
        return ResponseEntity.status(status).body(message)
    }

    @GetMapping(value = "/GetEligibilities", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun GetEligibilities(): List<EligibilitySimpleObj> {
        val statesAndRefs = rpcOps.vaultQuery(EligibilityState::class.java).states
        return statesAndRefs
                .map { stateAndRef -> stateAndRef.state.data }
                .map { state ->
                    // We map the anonymous lender and borrower to well-known identities if possible.
                    val possiblyWellKnownCreditRatingAgency = rpcOps.wellKnownPartyFromAnonymous(state.creditRatingAgency)
                            ?: state.creditRatingAgency
                    val possiblyWellKnownBank = rpcOps.wellKnownPartyFromAnonymous(state.bank) ?: state.bank

                    EligibilityState(
                            state.name,
                            possiblyWellKnownCreditRatingAgency,
                            possiblyWellKnownBank,
                            state.cibilRating,
                            state.loanId,
                            state.linearId).toSimpleObj()
                }
    }
}