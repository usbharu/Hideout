package dev.usbharu.hideout.core.infrastructure.springframework

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Component
class SPAInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (request.getParameter("s") == "f") {
            request.session.setAttribute("s", "f")
        } else if (request.getParameter("s") == "t") {
            request.session.setAttribute("s", "t")
        }
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        if (request.session.getAttribute("s") == "f") {
            return
        }
        val title = modelAndView?.modelMap?.getOrDefault("title", "")
        val url = modelAndView?.modelMap?.getOrDefault(
            "url",
            ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString() + '?' + request.queryString
        )
        val description = modelAndView?.modelMap?.getOrDefault("description", "")
        val image = modelAndView?.modelMap?.get("image")

        modelAndView?.clear()
        modelAndView?.addObject("nsUrl", request.requestURI + "?s=f" + request.queryString?.let { "&$it" }.orEmpty())
        modelAndView?.addAllObjects(mapOf("title" to title, "url" to url, "description" to description))
        image?.let { modelAndView?.addObject("image", it) }
        modelAndView?.viewName = "index"
    }
}