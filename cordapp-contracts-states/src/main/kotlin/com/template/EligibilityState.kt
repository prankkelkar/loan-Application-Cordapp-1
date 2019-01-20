package com.template

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
data class EligibilityState(val name: String,
                            val bank: Party,
                            val creditRatingAgency : Party,
                            val cibilRating: Int?,
                            val loanId: UniqueIdentifier,
                            override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {

    override val participants: List<AbstractParty> = listOf(creditRatingAgency,bank)
}