package dev.usbharu.hideout.core.interfaces.api.media

import dev.usbharu.hideout.application.config.LocalStorageConfig
import dev.usbharu.hideout.core.service.media.FileTypeDeterminationService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.nio.file.Path
import kotlin.io.path.name

@Controller
@ConditionalOnProperty("hideout.storage.type", havingValue = "local", matchIfMissing = true)
class LocalFileController(
    localStorageConfig: LocalStorageConfig,
    private val fileTypeDeterminationService: FileTypeDeterminationService
) {

    private val savePath = Path.of(localStorageConfig.path).toAbsolutePath()

    @GetMapping("/files/{id}")
    fun files(@PathVariable("id") id: String): ResponseEntity<Resource> {

        val name = Path.of(id).name
        val path = savePath.resolve(name)

        val mimeType = fileTypeDeterminationService.fileType(path, name)
        val pathResource = PathResource(path)

        return ResponseEntity
            .ok()
            .contentType(MediaType(mimeType.type, mimeType.subtype))
            .contentLength(pathResource.contentLength())
            .body(pathResource)
    }

    @GetMapping("/users/{user}/icon.jpg", "/users/{user}/header.jpg")
    fun icons(): ResponseEntity<Resource> {

        val pathResource = ClassPathResource("icon.png")
        return ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_PNG)
            .contentLength(pathResource.contentLength())
            .body(pathResource)
    }
}
