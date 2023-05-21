package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ap.Person

interface ActivityPubUserService {
    suspend fun getPersonByName(name: String): Person

    /**
     * Fetch person
     *
     * @param url
     * @param targetActor 署名するユーザー
     * @return
     */
    suspend fun fetchPerson(url: String, targetActor: String? = null): Person
}
