package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ap.Person

interface APUserService {
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
