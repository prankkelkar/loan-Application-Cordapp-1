package net.corda.server.controllers

import com.template.LoanRequestFlow
import com.template.LoanSchemaV1
import com.template.LoanState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.getOrThrow
import net.corda.server.NodeRPCConnection
import net.corda.server.models.LoanSimpleObj
import net.corda.server.models.toSimpleObj
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Define CorDapp-specific endpoints in a controller such as this.
 */
@RestController
@RequestMapping("/loan") // The paths for GET and POST requests are relative to this base path.
class LoanController(private val rpc: NodeRPCConnection) {

    private val rpcOps = rpc.proxy

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @PostMapping(value = "/LoanRequest", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    private fun LoanRequest(@RequestParam(value = "name") name: String,
                            @RequestParam(value = "amount") amount: Int,
                            @RequestParam(value = "bank") bank: String) : ResponseEntity<String>{

        // 1. Get party objects for the counterparty.
        val bankIdentity = rpcOps.partiesFromName(bank, exactMatch = false).singleOrNull()
                ?: throw IllegalStateException("Couldn't lookup node identity for $bank.")

        // 2. Start the LoanRequest flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    LoanRequestFlow::class.java,
                    name,
                    amount,
                    bankIdentity
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // 3. Return the result.
        return ResponseEntity.status(status).body(message)
    }

    @PostMapping(value = "/LoanApproval", produces = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    private fun LoanApproval(@RequestParam(value = "eligibilityID") eligibilityID: String,
                            @RequestParam(value = "loanstatus") loanStatus: String) : ResponseEntity<String>{

        // 1. Get linearId from string
        val linearID = UniqueIdentifier.fromString(eligibilityID)

        // 2. Start the LoanRequest flow. We block and wait for the flow to return.
        val (status, message) = try {
            val flowHandle = rpcOps.startFlowDynamic(
                    LoanRequestFlow::class.java,
                    linearID,
                    loanStatus.toBoolean()
            )

            val result = flowHandle.use { it.returnValue.getOrThrow() }
            HttpStatus.CREATED to "Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single().data}"
        } catch (e: Exception) {
            HttpStatus.BAD_REQUEST to e.message
        }

        // 3. Return the result.
        return ResponseEntity.status(status).body(message)
    }

    @GetMapping(value = "/GetLoans", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun getLoans(): List<LoanSimpleObj> {
        val statesAndRefs = rpcOps.vaultQuery(LoanState::class.java).states
        return statesAndRefs
                .map { stateAndRef -> stateAndRef.state.data }
                .map { state ->
                    // We map the anonymous lender and borrower to well-known identities if possible.
                    val possiblyWellKnownFinanceAgency = rpcOps.wellKnownPartyFromAnonymous(state.financeAgency)
                            ?: state.financeAgency
                    val possiblyWellKnownBank = rpcOps.wellKnownPartyFromAnonymous(state.bank) ?: state.bank

                    LoanState(
                            state.name,
                            state.amount,
                            possiblyWellKnownFinanceAgency,
                            possiblyWellKnownBank,
                            state.cibilRating,
                            state.loanStatus,
                            state.linearId).toSimpleObj()
                }
    }

    @GetMapping(value = "/GetApprovedLoans", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun getApprovedLoans(): List<LoanSimpleObj> {

        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val results = builder {
            var loanStatus = LoanSchemaV1.PersistentLoan::loanStatus.equal(true)
            val customCriteria1 = QueryCriteria.VaultCustomQueryCriteria(loanStatus)
            var partyType = LoanSchemaV1.PersistentLoan::financeAgency.equal(rpcOps.nodeInfo().legalIdentities.first().name.toString())
            val customCriteria2 = QueryCriteria.VaultCustomQueryCriteria(partyType)
            val criteria = generalCriteria.and(customCriteria1).and(customCriteria2)
            val results = rpcOps.vaultQueryBy<LoanState>(criteria).states

            return results
                    .map { stateAndRef -> stateAndRef.state.data }
                    .map { state ->
                        // We map the anonymous lender and borrower to well-known identities if possible.
                        val possiblyWellKnownFinanceAgency = rpcOps.wellKnownPartyFromAnonymous(state.financeAgency)
                                ?: state.financeAgency
                        val possiblyWellKnownBank = rpcOps.wellKnownPartyFromAnonymous(state.bank) ?: state.bank

                        LoanState(
                                state.name,
                                state.amount,
                                possiblyWellKnownFinanceAgency,
                                possiblyWellKnownBank,
                                state.cibilRating,
                                state.loanStatus,
                                state.linearId).toSimpleObj()
                    }

        }

    }
}