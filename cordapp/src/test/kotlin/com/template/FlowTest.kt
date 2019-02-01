package com.template

import com.google.common.collect.ImmutableList
import com.template.Contract.EligibilityContract
import com.template.State.EligibilityState
import com.template.State.LoanState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.*


class VerifyCheckEligibilityFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var nodeA: StartedMockNode
    private lateinit var nodeB: StartedMockNode
    private lateinit var nodeC: StartedMockNode
    private lateinit var loanId: UniqueIdentifier
    private lateinit var eligibilityId: UniqueIdentifier

    @Before
    fun setup() {
        network = MockNetwork(ImmutableList.of("com.template"))
        nodeA = network.createPartyNode(null)
        nodeB = network.createPartyNode(null)
        nodeC = network.createPartyNode(null)
        network.runNetwork()

        // Run the Loan Request Flow first
        val flow = LoanRequestFlow("Jhon",99 ,"abcd", nodeB.info.legalIdentities[0])
        val loanRequestFlowFuture = nodeA.startFlow(flow)
        network.runNetwork()
        val results = loanRequestFlowFuture.getOrThrow()
        val newLoanState = results.tx.outputs.single().data as LoanState
        loanId = newLoanState.linearId


        // Run the Check Eligibility Flow first
        val flow2 = verifyCheckEligibilityFlow(loanId,nodeC.info.legalIdentities[0])
        val loanRequestFlowFuture2 = nodeB.startFlow(flow2)
        network.runNetwork()
        val results2 = loanRequestFlowFuture2.getOrThrow()
        val newEligibilityState = results2.tx.outputs.single().data as EligibilityState
        eligibilityId = newEligibilityState.linearId


        val flow3 = verifyEligibilityApprovalFlow(eligibilityId)
        val verifyCheckEligibilityFlowFuture = nodeC.startFlow(flow3)
        network.runNetwork()
        val results3 = verifyCheckEligibilityFlowFuture.getOrThrow()

    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    @Throws(Exception::class)
    fun transactionConstructedByFlowUsesTheCorrectNotary() {
//        val verifyEligibilityApproval = verifyEligibilityApprovalFlow(eligibilityId)
//        val verifyCheckEligibilityFlowFuture = nodeC.startFlow(verifyEligibilityApproval)
//        network.runNetwork()
//        val signedTransaction = verifyCheckEligibilityFlowFuture.get()
//
//        assertEquals(1, signedTransaction.tx.outputStates.size.toLong())
//        val output = signedTransaction.tx.outputs[0]
//
//        assertEquals(network.notaryNodes[0].info.legalIdentities[0], output.notary)
    }

}