@file:Suppress("ktlint")
package gg.jte.generated.ondemand.orders
import com.space.subadmin.orders.OderListItemDto
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteorderslistGenerated {
companion object {
	@JvmField val JTE_NAME = "orders/orders-list.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,2,2,2,2,2,8,8,8,13,13,51,51,55,55,56,56,59,59,59,62,62,62,65,65,65,68,68,68,71,71,71,75,75,75,75,78,78,87,87,87,2,2,2,2,2)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, orders:List<OderListItemDto>) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html>\n\n<head>\n    ")
		gg.jte.generated.ondemand.fragments.JteheaderGenerated.render(jteOutput, jteHtmlInterceptor, "Order List");
		jteOutput.writeContent("\n</head>\n\n<body class=\"bg-gray-100\">\n\n")
		gg.jte.generated.ondemand.fragments.JtenavbarGenerated.render(jteOutput, jteHtmlInterceptor);
		jteOutput.writeContent("\n\n<div class=\"container mx-auto p-8\">\n    <div class=\"bg-white p-8 rounded-lg shadow-lg\">\n        <div class=\"flex justify-between items-center mb-6\">\n            <h1 class=\"text-2xl font-bold text-gray-800\">Order List</h1>\n        </div>\n        <div class=\"overflow-x-auto\">\n            <table class=\"min-w-full divide-y divide-gray-200\">\n                <thead class=\"bg-gray-50\">\n                <tr>\n                    <th class=\"px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider\"\n                        scope=\"col\">\n                        Order ID\n                    </th>\n                    <th class=\"px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider\"\n                        scope=\"col\">\n                        Customer\n                    </th>\n                    <th class=\"px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider\"\n                        scope=\"col\">\n                        Order Date\n                    </th>\n                    <th class=\"px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider\"\n                        scope=\"col\">\n                        Total\n                    </th>\n                    <th class=\"px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider\"\n                        scope=\"col\">\n                        Status\n                    </th>\n                    <th class=\"px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider\"\n                        scope=\"col\">\n                        Actions\n                    </th>\n                </tr>\n                </thead>\n                <tbody class=\"bg-white divide-y divide-gray-200\">\n                ")
		if (orders.isEmpty()) {
			jteOutput.writeContent("\n                    <tr>\n                        <td colspan=\"6\" class=\"text-center py-4 text-gray-500 italic\">No orders found.</td>\n                    </tr>\n                ")
		}
		jteOutput.writeContent("\n                ")
		for (order in orders) {
			jteOutput.writeContent("\n                    <tr class=\"hover:bg-gray-50\">\n                        <td class=\"px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900\">\n                            ")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(order.uuid)
			jteOutput.writeContent("\n                        </td>\n                        <td class=\"px-6 py-4 whitespace-nowrap text-sm text-gray-500\">\n                            ")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(order.customerName)
			jteOutput.writeContent("\n                        </td>\n                        <td class=\"px-6 py-4 whitespace-nowrap text-sm text-gray-500\">\n                            ")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(order.orderDate)
			jteOutput.writeContent("\n                        </td>\n                        <td class=\"px-6 py-4 whitespace-nowrap text-sm text-gray-500\">\n                            ")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(order.totalAmount)
			jteOutput.writeContent("\n                        </td>\n                        <td class=\"px-6 py-4 whitespace-nowrap text-sm text-gray-500\">\n                            ")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(order.status)
			jteOutput.writeContent("\n                        </td>\n                        <td class=\"px-6 py-4 whitespace-nowrap text-sm font-medium\">\n                            <a class=\"text-indigo-600 hover:text-indigo-900\"\n                               href=\"/orders/")
			jteOutput.setContext("a", "href")
			jteOutput.writeUserContent(order.uuid)
			jteOutput.setContext("a", null)
			jteOutput.writeContent("\">View</a>\n                        </td>\n                    </tr>\n                ")
		}
		jteOutput.writeContent("\n                </tbody>\n            </table>\n        </div>\n    </div>\n</div>\n\n</body>\n</html>\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val orders = params["orders"] as List<OderListItemDto>
		render(jteOutput, jteHtmlInterceptor, orders);
	}
}
}
