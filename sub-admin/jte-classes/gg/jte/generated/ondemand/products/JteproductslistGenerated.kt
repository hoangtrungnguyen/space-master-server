@file:Suppress("ktlint")
package gg.jte.generated.ondemand.products
import com.space.subadmin.products.ProductVariantInventoryDTO
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteproductslistGenerated {
companion object {
	@JvmField val JTE_NAME = "products/products-list.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,6,6,6,9,9,37,37,40,40,41,41,43,43,43,44,44,44,45,45,45,47,47,48,48,48,49,49,51,51,55,55,57,57,59,59,62,62,62,62,62,62,62,62,66,66,73,73,73,1,1,1,1,1)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, products:List<ProductVariantInventoryDTO>) {
		jteOutput.writeContent("\r\n<!DOCTYPE html>\r\n<html lang=\"en\">\r\n<head>\r\n    ")
		gg.jte.generated.ondemand.fragments.JteheaderGenerated.render(jteOutput, jteHtmlInterceptor, "Default Title");
		jteOutput.writeContent("\r\n</head>\r\n<body class=\"bg-gray-100 font-sans leading-normal tracking-normal\">\r\n")
		gg.jte.generated.ondemand.fragments.JtenavbarGenerated.render(jteOutput, jteHtmlInterceptor);
		jteOutput.writeContent("\r\n\r\n<div class=\"container mx-auto p-8\">\r\n    <div class=\"flex justify-between items-center mb-6\">\r\n        <h1 class=\"text-3xl font-bold text-gray-800\">Products</h1>\r\n        <div>\r\n            <a href=\"/products/add\"\r\n               class=\"bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-full transition duration-300 ease-in-out\">\r\n                Add New Product\r\n            </a>\r\n            <a href=\"/product-variants/add\"\r\n               class=\"bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-full transition duration-300 ease-in-out\">\r\n                Add New Product Variant\r\n            </a>\r\n        </div>\r\n    </div>\r\n    <div class=\"bg-white shadow-md rounded-lg overflow-hidden\">\r\n        <table class=\"min-w-full leading-normal\">\r\n            <thead>\r\n            <tr class=\"bg-gray-200 text-gray-600 uppercase text-sm leading-normal\">\r\n                <th class=\"py-3 px-6 text-left\">Product Name</th>\r\n                <th class=\"py-3 px-6 text-left\">SKU</th>\r\n                                        <th class=\"py-3 px-6 text-left\">Category</th>                <th class=\"py-3 px-6 text-right\">Quantity on Hand</th>\r\n                <th class=\"py-3 px-6 text-center\">Status</th>\r\n                <th class=\"py-3 px-6 text-center\">Actions</th>\r\n            </tr>\r\n            </thead>\r\n            <tbody class=\"text-gray-600 text-sm font-light\">\r\n            ")
		if (products.isEmpty()) {
			jteOutput.writeContent("\r\n                <tr>\r\n                                                <td colspan=\"6\" class=\"text-center py-4 text-gray-500 italic\">No products found.</td>                </tr>\r\n            ")
		}
		jteOutput.writeContent("\r\n            ")
		for (item in products) {
			jteOutput.writeContent("\r\n                <tr class=\"border-b border-gray-200 hover:bg-gray-100\">\r\n                    <td class=\"py-3 px-6 text-left\">")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(item.productName)
			jteOutput.writeContent("</td>\r\n                    <td class=\"py-3 px-6 text-left font-mono\">")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(item.variantSku)
			jteOutput.writeContent("</td>\r\n                    <td class=\"py-3 px-6 text-left\">")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(item.categoryName ?: "N/A")
			jteOutput.writeContent("</td>\r\n                    <td class=\"py-3 px-6 text-right font-semibold\">\r\n                    <span>  ")
			if (item.quantityOnHand != null) {
				jteOutput.writeContent("\r\n                            ")
				jteOutput.setContext("span", null)
				jteOutput.writeUserContent(item.quantityOnHand.toString())
				jteOutput.writeContent("\r\n                        ")
			} else {
				jteOutput.writeContent("\r\n                            \"N/A\"\r\n                        ")
			}
			jteOutput.writeContent("\r\n                             </span>\r\n                    </td>\r\n                    <td class=\"py-3 px-6 text-center\">\r\n                        ")
			if (item.isActive) {
				jteOutput.writeContent("\r\n                            <span class=\"px-2 py-1 text-xs font-semibold text-green-800 bg-green-200 rounded-full\">Active</span>\r\n                        ")
			} else {
				jteOutput.writeContent("\r\n                            <span class=\"px-2 py-1 text-xs font-semibold text-red-800 bg-red-200 rounded-full\">Inactive</span>\r\n                        ")
			}
			jteOutput.writeContent("\r\n                    </td>\r\n                    <td class=\"py-3 px-6 text-center\">\r\n                        <a href=\"/products/")
			jteOutput.setContext("a", "href")
			jteOutput.writeUserContent(item.productId)
			jteOutput.setContext("a", null)
			jteOutput.writeContent("?variantId=")
			jteOutput.setContext("a", "href")
			jteOutput.writeUserContent(item.variantId)
			jteOutput.setContext("a", null)
			jteOutput.writeContent("\"\r\n                           class=\"text-indigo-600 hover:text-indigo-900\">View Details</a>\r\n                    </td>\r\n                </tr>\r\n            ")
		}
		jteOutput.writeContent("\r\n            </tbody>\r\n        </table>\r\n    </div>\r\n</div>\r\n</body>\r\n</html>\r\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val products = params["products"] as List<ProductVariantInventoryDTO>
		render(jteOutput, jteHtmlInterceptor, products);
	}
}
}
