@file:Suppress("ktlint")
package gg.jte.generated.ondemand.fragments
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteheaderGenerated {
companion object {
	@JvmField val JTE_NAME = "fragments/header.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,0,0,0,0,2,2,2,2,4,4,4,0,0,0,0,0)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, title:String = "Default Title") {
		jteOutput.writeContent("<meta charset=\"UTF-8\">\r\n<title>")
		jteOutput.setContext("title", null)
		jteOutput.writeUserContent(title)
		jteOutput.writeContent("</title>\r\n<link rel=\"stylesheet\" href=\"/css/main.css\">\r\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val title = params["title"] as String? ?: "Default Title"
		render(jteOutput, jteHtmlInterceptor, title);
	}
}
}
