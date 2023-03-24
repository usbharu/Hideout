package dev.usbharu.hideout.ap

open class Object : JsonLd {
    private var type: List<String> = emptyList()
    private var name: String? = null

    protected constructor()
    constructor(type: List<String>, name: String) : super() {
        this.type = type
        this.name = name
    }

    companion object {
        @JvmStatic
        protected fun add(list:List<String>,type:String):List<String> {
            list.toMutableList().add(type)
            return list.distinct()
        }
    }
}
