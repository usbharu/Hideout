package dev.usbharu.hideout.core.infrastructure.springframework

import dev.usbharu.hideout.core.interfaces.web.common.OGP
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

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
        if (modelAndView?.viewName == "error") {
            return
        }

        if (request.getSession(false)?.getAttribute("s") == "f") {
            return
        }

        val ogp = modelAndView?.modelMap?.get("ogp") as? OGP

        modelAndView?.clear()
        modelAndView?.addObject("nsUrl", request.requestURI + "?s=f" + request.queryString?.let { "&$it" }.orEmpty())
        modelAndView?.addObject("ogp", ogp)
        modelAndView?.viewName = "index"
    }
}
