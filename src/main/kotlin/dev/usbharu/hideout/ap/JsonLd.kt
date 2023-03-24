package dev.usbharu.hideout.ap

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
open class JsonLd {
    @JsonProperty("@context")
    var context:List<String> = emptyList()

    @JsonCreator
    constructor(context:List<String>){
        this.context = context
    }

    protected constructor()
}
