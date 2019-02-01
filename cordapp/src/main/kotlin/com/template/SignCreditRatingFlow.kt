package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.FilteredTransaction
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class SignCreditRatingFlow(val oracle: Party ,val ftx: FilteredTransaction):FlowLogic<TransactionSignature>(){
    @Suspendable
    override fun call(): TransactionSignature {
        val session = initiateFlow(oracle)
        return session.sendAndReceive<TransactionSignature>(ftx).unwrap { it }
    }


}