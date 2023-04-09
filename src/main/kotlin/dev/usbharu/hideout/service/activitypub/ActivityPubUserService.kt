package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.ap.Person

interface ActivityPubUserService {
    suspend fun getPersonByName(name:String):Person

    suspend fun fetchPerson(url:String):Person
}
