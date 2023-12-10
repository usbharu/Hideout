package dev.usbharu.hideout.core.external.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Reject
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class DeliverRejectJobParam(
    val reject: Reject,
    val inbox: String,
    val signer: Long
)

@Component
class DeliverRejectJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverRejectJobParam, DeliverRejectJob>() {
    val reject = string("reject")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverRejectJobParam): ScheduleContext<DeliverRejectJob>.(DeliverRejectJob) -> Unit =
        {
            props[reject] = objectMapper.writeValueAsString(value.reject)
            props[inbox] = value.inbox
            props[signer] = value.signer
        }

    override fun convert(props: JobProps<DeliverRejectJob>): DeliverRejectJobParam = DeliverRejectJobParam(
        objectMapper.readValue<Reject>(props[reject]),
        props[inbox],
        props[signer]
    )
}
