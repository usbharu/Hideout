package dev.usbharu.hideout.core.external.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Delete
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class DeliverDeleteJobParam(
    val delete: Delete,
    val inbox: String,
    val signer: Long
)

@Component
class DeliverDeleteJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverDeleteJobParam, DeliverDeleteJob>("DeliverDeleteJob") {

    val delete = string("delete")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverDeleteJobParam): ScheduleContext<DeliverDeleteJob>.(DeliverDeleteJob) -> Unit = {
        props[delete] = objectMapper.writeValueAsString(value.delete)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverDeleteJob>): DeliverDeleteJobParam = DeliverDeleteJobParam(
        objectMapper.readValue(props[delete]),
        props[inbox],
        props[signer]
    )
}
