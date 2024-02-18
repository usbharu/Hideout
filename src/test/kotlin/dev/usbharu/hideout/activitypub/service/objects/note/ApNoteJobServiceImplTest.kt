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

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package dev.usbharu.hideout.activitypub.service.objects.note

class ApNoteJobServiceImplTest {
//    @Test
//    fun `createPostJob 新しい投稿のJob`() = runTest {
//        val apRequestService = mock<APRequestService>()
//        val user = UserBuilder.localUserOf()
//        val userQueryService = mock<UserQueryService> {
//            onBlocking { findByUrl(eq(user.url)) } doReturn user
//        }
//        val activityPubNoteService = ApNoteJobServiceImpl(
//
//            userQueryService = userQueryService,
//            apRequestService = apRequestService,
//            objectMapper = JsonObjectMapper.objectMapper,
//            transaction = TestTransaction
//        )
//        val remoteUserOf = UserBuilder.remoteUserOf()
//        activityPubNoteService.createNoteJob(
//            JobProps(
//                data = mapOf<String, Any>(
//                    DeliverPostJob.actor.name to user.url,
//                    DeliverPostJob.post.name to """{
//                          "id": 1,
//                          "userId": ${user.id},
//                          "text": "test text",
//                          "createdAt": 132525324,
//                          "visibility": 0,
//                          "url": "https://example.com"
//                        }""",
//                    DeliverPostJob.inbox.name to remoteUserOf.inbox,
//                    DeliverPostJob.media.name to "[]"
//                ), json = Json
//            )
//        )
//
//        val note = Note(
//            name = "Note",
//            id = "https://example.com",
//            attributedTo = user.url,
//            content = "test text",
//            published = Instant.ofEpochMilli(132525324).toString(),
//            to = listOfNotNull(APNoteServiceImpl.public, user.followers)
//        )
//        val create = Create(
//            name = "Create Note",
//            `object` = note,
//            actor = note.attributedTo,
//            id = "https://example.com/create/note/1"
//        )
//        verify(apRequestService, times(1)).apPost(
//            eq(remoteUserOf.inbox),
//            eq(create),
//            eq(user)
//        )
//    }
}
