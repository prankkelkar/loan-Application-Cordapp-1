package com.template

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
data class LoanState(val name: String,
                     val amount: Int,
                     val financeAgency: Party,
                     val bank: Party,
                     val cibilRating: Int?,
                     val loanStatus: Boolean?,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    override val participants: List<AbstractParty> = listOf(financeAgency,bank)
}