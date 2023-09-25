package dev.usbharu.hideout.service.job

import dev.usbharu.hideout.domain.model.job.HideoutJob
import kjob.core.dsl.KJobFunctions
import org.springframework.stereotype.Service
import kjob.core.dsl.JobContextWithProps as JCWP
import kjob.core.dsl.JobRegisterContext as JRC

@Service
interface JobQueueWorkerService {
    fun init(defines: List<Pair<HideoutJob, JRC<HideoutJob, JCWP<HideoutJob>>.(HideoutJob) -> KJobFunctions<HideoutJob, JCWP<HideoutJob>>>>)
}
