@file:Suppress("ktlint")
package gg.jte.generated.ondemand.products
import com.space.subadmin.products.ProductFormDTO
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JteproducteditorGenerated {
companion object {
	@JvmField val JTE_NAME = "products/product-editor.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,1,1,1,1,10,10,10,10,17,17,17,17,17,17,17,17,17,20,20,20,26,26,26,26,26,26,32,38,38,38,38,38,38,38,38,38,46,46,46,46,46,46,46,46,46,55,55,55,59,79,85,85,85,1,2,3,3,3,3,3)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, productForm:ProductFormDTO, isEdit:Boolean = false, productId:Long? = null) {
		jteOutput.writeContent("\n\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <title>")
		jteOutput.setContext("title", null)
		jteOutput.writeUserContent(if(isEdit) "Edit Product" else "Add New Product")
		jteOutput.writeContent("</title>\n    <script src=\"https://cdn.tailwindcss.com\"></script>\n    <link rel=\"stylesheet\" href=\"/css/main.css\">\n</head>\n<body class=\"bg-gray-100 font-sans leading-normal tracking-normal\">\n\n<div class=\"container mx-auto p-8\">\n    <form method=\"post\"")
		val __jte_html_attribute_0 = if(isEdit && productForm.productId != null) "/products/edit/" + productId else "/products/add"
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" action=\"")
			jteOutput.setContext("form", "action")
			jteOutput.writeUserContent(__jte_html_attribute_0)
			jteOutput.setContext("form", null)
			jteOutput.writeContent("\"")
		}
		jteOutput.writeContent(">\n        <div class=\"flex justify-between items-center mb-6\">\n            <h1 class=\"text-2xl font-bold text-gray-800\">\n                ")
		jteOutput.setContext("h1", null)
		jteOutput.writeUserContent(if(isEdit) "Edit Product" else "Add New Product" )
		jteOutput.writeContent("\n            </h1>\n            <div>\n                <a class=\"font-medium text-indigo-600 hover:text-indigo-500 mr-4\" href=\"/products/list\">Back to Product List</a>\n                <button class=\"flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500\"\n                        type=\"submit\">\n                    ")
		if (isEdit) {
			jteOutput.writeContent("Update Product")
		} else {
			jteOutput.writeContent("Save Product")
		}
		jteOutput.writeContent("\n                </button>\n            </div>\n        </div>\n\n        <div class=\"grid grid-cols-2 gap-8\">\n            ")
		jteOutput.writeContent("\n            <div class=\"space-y-6\">\n                <div>\n                    <label class=\"block text-sm font-medium text-gray-700\" for=\"name\">Product Name:</label>\n                    <input class=\"mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm\"\n                           id=\"name\" name=\"name\" required\n                          ")
		val __jte_html_attribute_1 = productForm.name
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
			jteOutput.writeContent(" value=\"")
			jteOutput.setContext("input", "value")
			jteOutput.writeUserContent(__jte_html_attribute_1)
			jteOutput.setContext("input", null)
			jteOutput.writeContent("\"")
		}
		jteOutput.writeContent("\n                           type=\"text\"/>\n                </div>\n\n                <div>\n                    <label class=\"block text-sm font-medium text-gray-700\" for=\"categoryId\">Category:</label>\n                    <input class=\"mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm\"\n                           id=\"categoryId\" name=\"categoryId\"\n                          ")
		val __jte_html_attribute_2 = productForm.categoryId
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
			jteOutput.writeContent(" value=\"")
			jteOutput.setContext("input", "value")
			jteOutput.writeUserContent(__jte_html_attribute_2)
			jteOutput.setContext("input", null)
			jteOutput.writeContent("\"")
		}
		jteOutput.writeContent("\n                           type=\"number\"/>\n                </div>\n\n                <div>\n                    <label class=\"block text-sm font-medium text-gray-700\" for=\"description\">Description (Markdown):</label>\n                    <textarea\n                            class=\"mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm\"\n                            id=\"description\" name=\"description\"\n                            rows=\"20\" placeholder=\"Enter product description in Markdown format\">")
		jteOutput.setContext("textarea", null)
		jteOutput.writeUserContent(productForm.description)
		jteOutput.writeContent("</textarea>\n                </div>\n            </div>\n\n            ")
		jteOutput.writeContent("\n            <div>\n                <label class=\"block text-sm font-medium text-gray-700\">Preview:</label>\n                <div id=\"preview\" class=\"mt-1 p-3 border border-gray-300 rounded-md bg-white min-h-[400px] prose\"></div>\n            </div>\n        </div>\n    </form>\n</div>\n\n<script src=\"https://cdn.jsdelivr.net/npm/marked/marked.min.js\"></script>\n<script>\n    const descriptionTextarea = document.getElementById('description');\n    const previewDiv = document.getElementById('preview');\n\n    function updatePreview() {\n        previewDiv.innerHTML = marked.parse(descriptionTextarea.value);\n    }\n\n    descriptionTextarea.addEventListener('input', updatePreview);\n\n    ")
		jteOutput.writeContent("\n    updatePreview();\n</script>\n\n</body>\n\n</html>")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val productForm = params["productForm"] as ProductFormDTO
		val isEdit = params["isEdit"] as Boolean? ?: false
		val productId = params["productId"] as Long?
		render(jteOutput, jteHtmlInterceptor, productForm, isEdit, productId);
	}
}
}
