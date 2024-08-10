package dev.usbharu.hideout.core.domain.model.filter

import dev.usbharu.hideout.core.domain.model.post.Post

class FilteredPost(val post: Post, val filterResults: List<FilterResult>)
