package dev.usbharu.hideout.core.external.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Block
import dev.usbharu.hideout.activitypub.domain.model.Reject
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class DeliverBlockJobParam(
    val signer: Long,
    val block: Block,
    val reject: Reject,
    val inbox: String
)

@Component
class DeliverBlockJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverBlockJobParam, DeliverBlockJob>("DeliverBlockJob") {

    val block = string("block")
    val reject = string("reject")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverBlockJobParam): ScheduleContext<DeliverBlockJob>.(DeliverBlockJob) -> Unit = {
        props[block] = objectMapper.writeValueAsString(value.block)
        props[reject] = objectMapper.writeValueAsString(value.reject)
        props[reject] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverBlockJob>): DeliverBlockJobParam = DeliverBlockJobParam(
        props[signer],
        objectMapper.readValue(props[block]),
        objectMapper.readValue(props[reject]),
        props[inbox]
    )


}
