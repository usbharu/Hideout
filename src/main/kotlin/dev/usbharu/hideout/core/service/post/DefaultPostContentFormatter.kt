package dev.usbharu.hideout.core.service.post

import org.jsoup.Jsoup
import org.jsoup.nodes.Attributes
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import org.springframework.stereotype.Service

@Service
class DefaultPostContentFormatter : PostContentFormatter {
    override fun format(content: String): FormattedPostContent {
        val document =
            Jsoup.parseBodyFragment(content).getElementsByTag("body").first() ?: return FormattedPostContent("", "")

        val flattenHtml = document.childNodes().mapNotNull {
            if (it is Element) {
                if (it.tagName() == "p") {
                    p(it)
                } else {
                    p(Element("p").appendChildren(document.childNodes()))
                }
            } else if (it is TextNode) {
                Element("p").appendText(it.text())
            } else {
                null
            }
        }.filter { it.text().isNotBlank() }

        val formattedHtml = mutableListOf<Element>()

        for (element in flattenHtml) {
            var brCount = 0
            var prevIndex = 0
            val childNodes = element.childNodes()
            for ((index, childNode) in childNodes.withIndex()) {
                if (childNode is Element && childNode.tagName() == "br") {
                    brCount++
                } else if (brCount >= 2) {
                    formattedHtml.add(Element("p").appendChildren(childNodes.subList(prevIndex, index - brCount)))
                    prevIndex = index
                }
            }
            formattedHtml.add(Element("p").appendChildren(childNodes.subList(prevIndex, childNodes.size)))
        }


        val elements = Elements(formattedHtml)

        return FormattedPostContent(elements.outerHtml().replace("\n", ""), printHtml(elements))
    }

    private fun p(element: Element): Element {
        val childNodes = element.childNodes()

        if (childNodes.size == 1 && childNodes.first() is TextNode) {
            val pTag = Element("p")

            pTag.appendText(element.text())
            return pTag
        }

        val map = childNodes.mapNotNull {
            if (it is Element) {
                if (it.tagName() == "a") {
                    a(it)
                } else if (it.tagName() == "br") {
                    Element("br")
                } else {
                    TextNode(it.text())
                }
            } else if (it is TextNode) {
                it
            } else {
                null
            }
        }

        val pTag = Element("p")

        pTag.appendChildren(map)

        return pTag
    }

    private fun a(element: Element): Element {
        val attributes = Attributes()

        attributes.put("href", element.attribute("href").value)
        return Element(Tag.valueOf("a"), "", attributes).appendText(element.text())
    }

    private fun printHtml(element: Elements): String {
        return element.joinToString("\n\n") {
            it.childNodes().joinToString("") {
                if (it is Element && it.tagName() == "br") {
                    "\n"
                } else if (it is Element) {
                    it.text()
                } else if (it is TextNode) {
                    it.text()
                } else {
                    ""
                }
            }
        }
    }
}
