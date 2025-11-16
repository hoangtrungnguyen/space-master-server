@file:Suppress("ktlint")
package gg.jte.generated.ondemand.products
import com.space.subadmin.products.ProductVariantInventoryDTO
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteproductslistGenerated {
companion object {
	@JvmField val JTE_NAME = "products/products-list.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,6,6,6,9,9,37,37,40,40,41,41,43,43,43,44,44,44,45,45,45,47,47,48,48,48,49,49,51,51,55,55,57,57,59,59,62,62,62,62,62,62,62,62,66,66,73,73,73,1,1,1,1,1)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, products:List<ProductVariantInventoryDTO>) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    ")
		gg.jte.generated.ondemand.fragments.JteheaderGenerated.render(jteOutput, jteHtmlInterceptor, "Default Title");
		jteOutput.writeContent("\n</head>\n<body class=\"bg-gray-100 font-sans leading-normal tracking-normal\">\n")
		gg.jte.generated.ondemand.fragments.JtenavbarGenerated.render(jteOutput, jteHtmlInterceptor);
		jteOutput.writeContent("\n\n<div class=\"container mx-auto p-8\">\n    <div class=\"flex justify-between items-center mb-6\">\n        <h1 class=\"text-3xl font-bold text-gray-800\">Products</h1>\n        <div>\n            <a href=\"/products/add\"\n               class=\"bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-full transition duration-300 ease-in-out\">\n                Add New Product\n            </a>\n            <a href=\"/product-variants/add\"\n               class=\"bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-full transition duration-300 ease-in-out\">\n                Add New Product Variant\n            </a>\n        </div>\n    </div>\n    <div class=\"bg-white shadow-md rounded-lg overflow-hidden\">\n        <table class=\"min-w-full leading-normal\">\n            <thead>\n            <tr class=\"bg-gray-200 text-gray-600 uppercase text-sm leading-normal\">\n                <th class=\"py-3 px-6 text-left\">Product Name</th>\n                <th class=\"py-3 px-6 text-left\">SKU</th>\n                                        <th class=\"py-3 px-6 text-left\">Category</th>                <th class=\"py-3 px-6 text-right\">Quantity on Hand</th>\n                <th class=\"py-3 px-6 text-center\">Status</th>\n                <th class=\"py-3 px-6 text-center\">Actions</th>\n            </tr>\n            </thead>\n            <tbody class=\"text-gray-600 text-sm font-light\">\n            ")
		if (products.isEmpty()) {
			jteOutput.writeContent("\n                <tr>\n                                                <td colspan=\"6\" class=\"text-center py-4 text-gray-500 italic\">No products found.</td>                </tr>\n            ")
		}
		jteOutput.writeContent("\n            ")
		for (item in products) {
			jteOutput.writeContent("\n                <tr class=\"border-b border-gray-200 hover:bg-gray-100\">\n                    <td class=\"py-3 px-6 text-left\">")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(item.productName)
			jteOutput.writeContent("</td>\n                    <td class=\"py-3 px-6 text-left font-mono\">")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(item.variantSku)
			jteOutput.writeContent("</td>\n                    <td class=\"py-3 px-6 text-left\">")
			jteOutput.setContext("td", null)
			jteOutput.writeUserContent(item.categoryName ?: "N/A")
			jteOutput.writeContent("</td>\n                    <td class=\"py-3 px-6 text-right font-semibold\">\n                    <span>  ")
			if (item.quantityOnHand != null) {
				jteOutput.writeContent("\n                            ")
				jteOutput.setContext("span", null)
				jteOutput.writeUserContent(item.quantityOnHand.toString())
				jteOutput.writeContent("\n                        ")
			} else {
				jteOutput.writeContent("\n                            \"N/A\"\n                        ")
			}
			jteOutput.writeContent("\n                             </span>\n                    </td>\n                    <td class=\"py-3 px-6 text-center\">\n                        ")
			if (item.isActive) {
				jteOutput.writeContent("\n                            <span class=\"px-2 py-1 text-xs font-semibold text-green-800 bg-green-200 rounded-full\">Active</span>\n                        ")
			} else {
				jteOutput.writeContent("\n                            <span class=\"px-2 py-1 text-xs font-semibold text-red-800 bg-red-200 rounded-full\">Inactive</span>\n                        ")
			}
			jteOutput.writeContent("\n                    </td>\n                    <td class=\"py-3 px-6 text-center\">\n                        <a href=\"/products/")
			jteOutput.setContext("a", "href")
			jteOutput.writeUserContent(item.productId)
			jteOutput.setContext("a", null)
			jteOutput.writeContent("?variantId=")
			jteOutput.setContext("a", "href")
			jteOutput.writeUserContent(item.variantId)
			jteOutput.setContext("a", null)
			jteOutput.writeContent("\"\n                           class=\"text-indigo-600 hover:text-indigo-900\">View Details</a>\n                    </td>\n                </tr>\n            ")
		}
		jteOutput.writeContent("\n            </tbody>\n        </table>\n    </div>\n</div>\n</body>\n</html>\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val products = params["products"] as List<ProductVariantInventoryDTO>
		render(jteOutput, jteHtmlInterceptor, products);
	}
}
}
