package com.template

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for TradeState.
 */
object LoanSchema

/**
 * An TradeState schema.
 */
object LoanSchemaV1 : MappedSchema(
        schemaFamily = LoanSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentLoan::class.java)) {
    @Entity
    @Table(name = "loan_states")
    class PersistentLoan(
            @Column(name = "itemOwner")
            var name: String,

            @Column(name = "amount")
            var amount: Int,

            @Column(name = "financeAgency")
            var financeAgency: String,

            @Column(name = "bank")
            var bank: String,

            @Column(name = "cibilRating")
            var cibilRating: Int?,

            @Column(name = "loanStatus")
            var loanStatus: Boolean?,

            @Column(name = "linear_id")
            var linearId: String

    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", 0, "","",null, null, UUID.randomUUID().toString())
    }
}