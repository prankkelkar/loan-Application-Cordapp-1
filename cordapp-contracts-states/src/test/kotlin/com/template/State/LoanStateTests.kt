package com.template.State

import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import org.junit.Test
import kotlin.test.assertEquals

class LoanStateTests{
    val alice = TestIdentity(CordaX500Name("Alice", "", "GB")).party
    val bob = TestIdentity(CordaX500Name("Bob", "", "GB")).party

    @Test
    fun loanStateHasParamsOfCorrectTypeInConstructor() {
        LoanState("Jack",0, "PANCARD",alice, bob, 1, null)
    }

    @Test
    fun loanStateHasGettersForIssuerOwnerAndAmount() {
        var loanState = LoanState("Jack",0, "PANCARD",alice, bob, 1, null)
        assertEquals(alice, loanState.financeAgency)
        assertEquals(bob, loanState.bank)
        assertEquals(0, loanState.amount)
    }

    @Test
    fun loanStateImplementsContractState() {
        assert(LoanState("Jack",0, "PANCARD",alice, bob, 1, null) is ContractState)
    }

    @Test
    fun loanStateHasTwoParticipantsTheFinanceAgencyAndTheBank() {
        var tokenState = LoanState("Jack",0, "PANCARD",alice, bob, 1, null)
        assertEquals(2, tokenState.participants.size)
        assert(tokenState.participants.contains(alice))
        assert(tokenState.participants.contains(bob))
    }
}