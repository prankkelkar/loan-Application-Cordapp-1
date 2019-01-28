package com.template

import com.google.common.collect.ImmutableList
import com.template.Contract.LoanContract
import com.template.State.LoanState
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.*
import kotlin.test.assertEquals

class LoanRequestFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var nodeA: StartedMockNode
    private lateinit var nodeB: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(ImmutableList.of("test_network"))
        nodeA = network.createPartyNode(null)
        nodeB = network.createPartyNode(null)
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    @Throws(Exception::class)
    fun transactionConstructedByFlowUsesTheCorrectNotary() {
        val flow = LoanRequestFlow("Jhon",99 ,nodeB.info.legalIdentities[0])
        val future = nodeA.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()

        assertEquals(1, signedTransaction.tx.outputStates.size.toLong())
        val output = signedTransaction.tx.outputs[0]

        assertEquals(network.notaryNodes[0].info.legalIdentities[0], output.notary)
    }

//    @Test
//    @Throws(Exception::class)
//    fun transactionConstructedByFlowHasOneLoanStateOutputWithTheCorrectParameters() {
//        val flow = LoanRequestFlow("Jhon",99 ,nodeB.info.legalIdentities[0])
//        val future = nodeA.startFlow(flow)
//        network.runNetwork()
//        val signedTransaction = future.get()
//
//        assertEquals(1, signedTransaction.tx.outputStates.size.toLong())
//        val output = signedTransaction.tx.outputsOfType(LoanState::class.java)[0]
//
//        assertEquals(nodeA.info.legalIdentities[0], output.financeAgency)
//        assertEquals(nodeB.info.legalIdentities[0], output.bank)
//        assertEquals("Jhon", output.name)
//        assertEquals(99, output.amount)
//        assertEquals(null, output.cibilRating)
//        assertEquals(null, output.loanStatus)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun transactionConstructedByFlowHasOneOutputUsingTheCorrectContract() {
//        val flow = LoanRequestFlow("Jhon",99 ,nodeB.info.legalIdentities[0])
//        val future = nodeA.startFlow(flow)
//        network.runNetwork()
//        val signedTransaction = future.get()
//
//        assertEquals(1, signedTransaction.tx.outputStates.size.toLong())
//        val (_, contract) = signedTransaction.tx.outputs[0]
//
//        assertEquals("com.template.Contract.LoanContract", contract)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun transactionConstructedByFlowHasOneLoanRequestCommand() {
//        val flow = LoanRequestFlow("Jhon",99 ,nodeB.info.legalIdentities[0])
//        val future = nodeA.startFlow(flow)
//        network.runNetwork()
//        val signedTransaction = future.get()
//
//        assertEquals(1, signedTransaction.tx.commands.size.toLong())
//        val (value) = signedTransaction.tx.commands[0]
//
//        assert(value is LoanContract.Commands.LoanRequest)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun transactionConstructedByFlowHasOneCommandWithTheFinancyAgencyAsASigner() {
//        val flow = LoanRequestFlow("Jhon",99 ,nodeB.info.legalIdentities[0])
//        val future = nodeA.startFlow(flow)
//        network.runNetwork()
//        val signedTransaction = future.get()
//
//        assertEquals(1, signedTransaction.tx.commands.size.toLong())
//        val (_, signers) = signedTransaction.tx.commands[0]
//
//        assertEquals(1, signers.size.toLong())
//        assert(signers.contains(nodeA.info.legalIdentities[0].owningKey))
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun transactionConstructedByFlowHasNoInputsAttachmentsOrTimeWindows() {
//        val flow = LoanRequestFlow("Jhon",99 ,nodeB.info.legalIdentities[0])
//        val future = nodeA.startFlow(flow)
//        network.runNetwork()
//        val signedTransaction = future.get()
//
//        assertEquals(0, signedTransaction.tx.inputs.size.toLong())
//        // The single attachment is the contract attachment.
//        assertEquals(1, signedTransaction.tx.attachments.size.toLong())
//        assertEquals(null, signedTransaction.tx.timeWindow)
//    }
}