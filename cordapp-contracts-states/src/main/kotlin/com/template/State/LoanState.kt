package com.template.State

import com.template.Schema.LoanSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

// *********
// * State *
// *********
data class LoanState(val name: String,
                     val amount: Int,
                     val panCardNo: String,
                     val financeAgency: Party,
                     val bank: Party,
                     val cibilRating: Int?,
                     val loanStatus: Boolean?,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState, QueryableState {

    override val participants: List<AbstractParty> = listOf(financeAgency,bank)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is LoanSchemaV1 -> LoanSchemaV1.PersistentLoan(
                    this.name,
                    this.amount,
                    this.panCardNo,
                    this.financeAgency.name.toString(),
                    this.bank.name.toString(),
                    this.cibilRating,
                    this.loanStatus,
                    this.linearId.id.toString()
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(LoanSchemaV1)
}