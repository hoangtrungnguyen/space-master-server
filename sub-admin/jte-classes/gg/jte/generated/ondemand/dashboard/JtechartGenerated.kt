@file:Suppress("ktlint")
package gg.jte.generated.ondemand.dashboard
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class JtechartGenerated {
companion object {
	@JvmField val JTE_NAME = "dashboard/chart.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,0,0,0,0,5,13,23,25,26,31,34,34,35,39,43,43,43,49,63,63,71,71,71,81,81,81,0,1,3,4,4,4,4,4)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, labelsJson:String, dataJson:String, transactionLabels:String, transactionData:String) {
		jteOutput.writeContent("\n<div class=\"lg:col-span-3 bg-white p-6 rounded-lg shadow-md\">\n    <h2 class=\"text-xl font-semibold text-gray-700 mb-4\">Revenue & Profit Margin</h2>\n    <div class=\"h-80\">\n        <canvas id=\"revenueProfitChart\"></canvas>\n    </div>\n</div>\n\n")
		jteOutput.writeContent("\n<div class=\"lg:col-span-2 bg-white p-6 rounded-lg shadow-md\">\n    <h2 class=\"text-xl font-semibold text-gray-700 mb-4\">Transactions</h2>\n    <div class=\"h-80\">\n        <canvas id=\"transactionsChart\"></canvas>\n    </div>\n</div>\n\n\n<script>\n    ")
		jteOutput.writeContent("\n    (function () {\n        ")
		jteOutput.writeContent("\n        ")
		jteOutput.writeContent("\n        if (window.myRevenueChart) {\n            window.myRevenueChart.destroy();\n        }\n\n        ")
		jteOutput.writeContent("\n        var ctx = document.getElementById('revenueProfitChart').getContext('2d');\n\n        const labelData = ")
		jteOutput.writeUnsafeContent(labelsJson)
		jteOutput.writeContent(";\n        ")
		jteOutput.writeContent("\n        window.myRevenueChart = new Chart(ctx, {\n            type: 'line',\n            data: {\n                ")
		jteOutput.writeContent("\n                labels: labelData,\n                datasets: [{\n                    label: 'Revenue',\n                    data: ")
		jteOutput.setContext("script", null)
		jteOutput.writeUserContent( dataJson)
		jteOutput.writeContent(",\n                    borderColor: 'rgb(75, 192, 192)',\n                    tension: 0.1\n                }]\n            },\n            options: {\n                animation: false ")
		jteOutput.writeContent("\n            }\n        });\n    })();\n</script>\n\n<script>\n    (function () {\n        if (window.transactionChart) {\n            window.transactionChart.destroy();\n        }\n\n        var ctx = document.getElementById('transactionsChart').getContext('2d');\n\n        const labelTransactions = ")
		jteOutput.writeUnsafeContent(transactionLabels)
		jteOutput.writeContent(";\n\n        window.transactionChart = new Chart(ctx, {\n            type: 'bar',\n            data: {\n                labels: labelTransactions,\n                datasets: [{\n                    label: 'Transaction',\n                    data: ")
		jteOutput.setContext("script", null)
		jteOutput.writeUserContent( transactionData)
		jteOutput.writeContent(",\n                    borderColor: 'rgb(75, 192, 192)',\n                }]\n            },\n            options: {\n                responsive: true,\n                maintainAspectRatio: false,\n            }\n        });\n    })();\n</script>")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val labelsJson = params["labelsJson"] as String
		val dataJson = params["dataJson"] as String
		val transactionLabels = params["transactionLabels"] as String
		val transactionData = params["transactionData"] as String
		render(jteOutput, jteHtmlInterceptor, labelsJson, dataJson, transactionLabels, transactionData);
	}
}
}
