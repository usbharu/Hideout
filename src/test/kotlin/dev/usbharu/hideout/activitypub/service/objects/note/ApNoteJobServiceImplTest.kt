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
