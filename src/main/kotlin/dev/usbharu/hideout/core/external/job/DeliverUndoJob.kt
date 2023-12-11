package dev.usbharu.hideout.core.external.job

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.usbharu.hideout.activitypub.domain.model.Undo
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

data class DeliverUndoJobParam(
    val undo: Undo,
    val inbox: String,
    val signer: Long
)

@Component
class DeliverUndoJob(@Qualifier("activitypub") private val objectMapper: ObjectMapper) :
    HideoutJob<DeliverUndoJobParam, DeliverUndoJob>("DeliverUndoJob") {

    val undo = string("undo")
    val inbox = string("inbox")
    val signer = long("signer")

    override fun convert(value: DeliverUndoJobParam): ScheduleContext<DeliverUndoJob>.(DeliverUndoJob) -> Unit = {
        props[undo] = objectMapper.writeValueAsString(value.undo)
        props[inbox] = value.inbox
        props[signer] = value.signer
    }

    override fun convert(props: JobProps<DeliverUndoJob>): DeliverUndoJobParam {
        return DeliverUndoJobParam(
            objectMapper.readValue(props[undo]),
            props[inbox],
            props[signer]
        )
    }
}
