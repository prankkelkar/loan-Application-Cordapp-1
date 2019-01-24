package net.corda.server.models

import com.template.State.EligibilityState
import com.template.State.LoanState
import net.corda.core.identity.Party

fun Party.toSimpleName(): String {
    return "${name.organisation} (${name.locality}, ${name.country})"
}

data class LoanSimpleObj(
        val name: String,
        val amount: String,
        val financeAgency: String,
        val bank: String,
        val cibilRating: String,
        val loanStatus: String,
        val linearId: String
)

fun LoanState.toSimpleObj(): LoanSimpleObj {
    return LoanSimpleObj(name.toString(),
            amount.toString() ,
            financeAgency.nameOrNull()!!.organisation ,
            bank.nameOrNull()!!.organisation ,
            cibilRating.toString(),
            loanStatus.toString(),
            linearId.id.toString())
}

data class EligibilitySimpleObj(
        val name: String,
        val creditRatingAgency: String,
        val bank: String,
        val cibilRating: String,
        val loanId: String,
        val linearId: String
)

fun EligibilityState.toSimpleObj(): EligibilitySimpleObj {
    return EligibilitySimpleObj(name.toString(),
            creditRatingAgency.nameOrNull()!!.organisation ,
            bank.nameOrNull()!!.organisation ,
            cibilRating.toString(),
            loanId.toString(),
            linearId.id.toString())
}