package com.template
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class QueryCreditRatingFlow(val oracle: Party, val PanCardNo: String): FlowLogic<Int>(){

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    override fun call(): Int {
        return initiateFlow(oracle).sendAndReceive<Int>(PanCardNo).unwrap{it}
    }

}