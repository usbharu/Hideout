package dev.usbharu.hideout.core.service.job

import kjob.core.dsl.KJobFunctions
import org.springframework.stereotype.Service
import dev.usbharu.hideout.core.external.job.HideoutJob as HJ
import kjob.core.dsl.JobContextWithProps as JCWP
import kjob.core.dsl.JobRegisterContext as JRC

@Service
interface JobQueueWorkerService {
    fun init(
        defines: List<Pair<HJ, JRC<HJ, JCWP<HJ>>.(HJ) -> KJobFunctions<HJ, JCWP<HJ>>>>
    )
}
