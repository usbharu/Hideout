/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.activitypub.service.activity.create

import dev.usbharu.hideout.activitypub.domain.model.Create
import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.service.common.AbstractActivityPubProcessor
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.activitypub.service.objects.note.APNoteService
import dev.usbharu.hideout.application.external.Transaction
import org.springframework.stereotype.Service

@Service
class CreateActivityProcessor(transaction: Transaction, private val apNoteService: APNoteService) :
    AbstractActivityPubProcessor<Create>(transaction) {
    override suspend fun internalProcess(activity: ActivityPubProcessContext<Create>) {
        apNoteService.fetchNote(activity.activity.apObject as Note)
    }

    override fun isSupported(activityType: ActivityType): Boolean = activityType == ActivityType.Create

    override fun type(): Class<Create> = Create::class.java
}
