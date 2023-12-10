package dev.usbharu.hideout.core.external.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Accept
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.stereotype.Component

data class DeliverAcceptJobParam(
    val accept: Accept,
    val inbox: String,
    val signer: Long
)

@Component
class DeliverAcceptJob(private val objectMapper: ObjectMapper) : HideoutJob<DeliverAcceptJobParam, DeliverAcceptJob>() {

    val accept = string("accept")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverAcceptJobParam): ScheduleContext<DeliverAcceptJob>.(DeliverAcceptJob) -> Unit = {
        props[accept] = objectMapper.writeValueAsString(value.accept)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverAcceptJob>): DeliverAcceptJobParam {
        return DeliverAcceptJobParam(
            objectMapper.readValue(props[accept]),
            props[inbox],
            props[signer]
        )
    }
}
