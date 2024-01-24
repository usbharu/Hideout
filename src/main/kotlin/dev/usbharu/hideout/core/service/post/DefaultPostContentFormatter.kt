package dev.usbharu.hideout.core.service.post

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import org.owasp.html.PolicyFactory
import org.springframework.stereotype.Service

@Service
class DefaultPostContentFormatter(private val policyFactory: PolicyFactory) : PostContentFormatter {
    override fun format(content: String): FormattedPostContent {

        //まず不正なHTMLを整形する
        val document = Jsoup.parseBodyFragment(content)
        val outputSettings = Document.OutputSettings()
        outputSettings.prettyPrint(false)

        document.outputSettings(outputSettings)

        val unsafeElement = document.getElementsByTag("body").first() ?: return FormattedPostContent(
            "",
            ""
        )


        //文字だけのHTMLなどはここでpタグで囲む
        val flattenHtml = unsafeElement.childNodes().mapNotNull {
            if (it is Element) {
                it
            } else if (it is TextNode) {
                Element("p").appendText(it.text())
            } else {
                null
            }
        }.filter { it.text().isNotBlank() }


        // HTMLのサニタイズをする
        val unsafeHtml = Elements(flattenHtml).outerHtml()

        val safeHtml = policyFactory.sanitize(unsafeHtml)

        val safeDocument =
            Jsoup.parseBodyFragment(safeHtml).getElementsByTag("body").first() ?: return FormattedPostContent("", "")

        val formattedHtml = mutableListOf<Element>()


        //連続するbrタグを段落に変換する
        for (element in safeDocument.children()) {
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
