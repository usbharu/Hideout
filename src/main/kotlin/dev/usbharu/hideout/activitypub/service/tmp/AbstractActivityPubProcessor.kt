package dev.usbharu.hideout.activitypub.service.tmp

import dev.usbharu.hideout.activitypub.domain.exception.ActivityPubProcessException
import dev.usbharu.hideout.activitypub.domain.exception.FailedProcessException
import dev.usbharu.hideout.activitypub.domain.exception.HttpSignatureUnauthorizedException
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.application.external.Transaction
import org.slf4j.LoggerFactory

abstract class AbstractActivityPubProcessor<T : Object>(
    private val transaction: Transaction,
    private val allowUnauthorized: Boolean = false
) : ActivityPubProcessor<T> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun process(activity: ActivityPubProcessContext<T>) {
        if (activity.isAuthorized.not() && allowUnauthorized.not()) {
            throw HttpSignatureUnauthorizedException()
        }
        logger.info("START ActivityPub process")
        try {
            transaction.transaction {
                internalProcess(activity)
            }
        } catch (e: ActivityPubProcessException) {
            logger.warn("FAILED ActivityPub process", e)
            throw FailedProcessException("Failed process", e)
        }
        logger.info("SUCCESS ActivityPub process")
    }

    abstract suspend fun internalProcess(activity: ActivityPubProcessContext<T>)

}
