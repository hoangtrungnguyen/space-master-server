@file:Suppress("ktlint")
package gg.jte.generated.ondemand.fragments
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JtenavbarGenerated {
companion object {
	@JvmField val JTE_NAME = "fragments/navbar.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(25,25,25,25,25,25,25,25,25,25,25,25,25,25,25)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?) {
		jteOutput.writeContent("<nav class=\"bg-gray-800 p-4\">\n    <div class=\"container mx-auto flex justify-between items-center\">\n        <div class=\"text-white text-lg font-bold\">\n            <a href=\"/\">Sub-Admin Dashboard</a>\n        </div>\n        <div class=\"flex-grow space-x-4 ml-4\">\n            <a class=\"text-gray-300 hover:text-white\" href=\"/products/list\">Products</a>\n            <a class=\"text-gray-300 hover:text-white\" href=\"/orders/list\">Orders</a>\n            <a class=\"text-gray-300 hover:text-white\" href=\"/inventory\">Inventory</a>\n            <a class=\"text-gray-300 hover:text-white\" href=\"/users/list\">Users</a>\n        </div>\n        <div class=\"flex items-center space-x-4\">\n            <div class=\"space-x-2\">\n                <a class=\"text-sm text-gray-300 hover:text-white\" href=\"?lang=en\">EN</a>\n                <span class=\"text-gray-500\">|</span>\n                <a class=\"text-sm text-gray-300 hover:text-white\" href=\"?lang=vi\">VI</a>\n            </div>\n            <div>\n                <form action=\"/logout\" method=\"post\" class=\"inline ml-4\">\n                    <button type=\"submit\" class=\"text-gray-300 hover:text-white\">Logout</button>\n                </form>\n            </div>\n        </div>\n    </div>\n</nav>\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
}
