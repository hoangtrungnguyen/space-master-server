@file:Suppress("ktlint")
package gg.jte.generated.ondemand.fragments
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JtenavbarGenerated {
companion object {
	@JvmField val JTE_NAME = "fragments/navbar.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(25,25,25,25,25,25,25,25,25,25,25,25,25,25,25)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?) {
		jteOutput.writeContent("<nav class=\"bg-gray-800 p-4\">\r\n    <div class=\"container mx-auto flex justify-between items-center\">\r\n        <div class=\"text-white text-lg font-bold\">\r\n            <a href=\"/\">Sub-Admin Dashboard</a>\r\n        </div>\r\n        <div class=\"flex-grow space-x-4 ml-4\">\r\n            <a class=\"text-gray-300 hover:text-white\" href=\"/products/list\">Products</a>\r\n            <a class=\"text-gray-300 hover:text-white\" href=\"/orders/list\">Orders</a>\r\n            <a class=\"text-gray-300 hover:text-white\" href=\"/inventory\">Inventory</a>\r\n            <a class=\"text-gray-300 hover:text-white\" href=\"/users/list\">Users</a>\r\n        </div>\r\n        <div class=\"flex items-center space-x-4\">\r\n            <div class=\"space-x-2\">\r\n                <a class=\"text-sm text-gray-300 hover:text-white\" href=\"?lang=en\">EN</a>\r\n                <span class=\"text-gray-500\">|</span>\r\n                <a class=\"text-sm text-gray-300 hover:text-white\" href=\"?lang=vi\">VI</a>\r\n            </div>\r\n            <div>\r\n                <form action=\"/logout\" method=\"post\" class=\"inline ml-4\">\r\n                    <button type=\"submit\" class=\"text-gray-300 hover:text-white\">Logout</button>\r\n                </form>\r\n            </div>\r\n        </div>\r\n    </div>\r\n</nav>\r\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
}
