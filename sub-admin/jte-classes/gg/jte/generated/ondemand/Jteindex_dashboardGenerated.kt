@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import com.space.subadmin.dashboard.PeriodMetricsUiDto
import com.space.subadmin.dashboard.GroupedChartDataUiDto
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class Jteindex_dashboardGenerated {
companion object {
	@JvmField val JTE_NAME = "index_dashboard.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,3,3,3,3,3,13,13,15,17,19,27,35,35,37,40,43,48,48,48,48,48,49,49,49,49,49,50,50,50,50,50,51,51,51,51,51,56,59,65,65,65,81,87,87,87,103,109,109,109,139,139,139,3,4,5,5,5,5,5)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, periodMetricDto:PeriodMetricsUiDto, days:Int = 30, groupedChartData:List<GroupedChartDataUiDto>) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Sales Performance Dashboard</title>\n    ")
		jteOutput.writeContent("\n    <script src=\"https://cdn.tailwindcss.com\"></script>\n    ")
		jteOutput.writeContent("\n    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n    ")
		jteOutput.writeContent("\n    <script src=\"https://cdn.jsdelivr.net/npm/chartjs-adapter-date-fns/dist/chartjs-adapter-date-fns.bundle.min.js\"></script>\n    ")
		jteOutput.writeContent("\n    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n    <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">\n\n    <script src=\"/webjars/htmx.org/1.9.10/dist/htmx.min.js\"></script>\n\n    <style>\n        ")
		jteOutput.writeContent("\n        body {\n            font-family: 'Inter', sans-serif;\n        }\n    </style>\n</head>\n<body class=\"bg-gray-100 antialiased\">\n\n")
		gg.jte.generated.ondemand.fragments.JtenavbarGenerated.render(jteOutput, jteHtmlInterceptor);
		jteOutput.writeContent("\n\n")
		jteOutput.writeContent("\n<div class=\"container mx-auto p-4 md:p-8\">\n\n    ")
		jteOutput.writeContent("\n    <header class=\"mb-8 flex flex-col sm:flex-row justify-between items-center\">\n        <h1 class=\"text-3xl font-bold text-gray-800\">Sales Performance Dashboard</h1>\n        ")
		jteOutput.writeContent("\n        <div>\n            <label for=\"dateRange\" class=\"text-sm font-medium text-gray-600 mr-2\">Period:</label>\n            <select id=\"dateRange\" name=\"dateRange\"\n                    class=\"mt-1 block w-full sm:w-auto pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm\">\n                <option value=\"30\"")
		val __jte_html_attribute_0 = days == 30
		if (__jte_html_attribute_0) {
		jteOutput.writeContent(" selected")
		}
		jteOutput.writeContent(">Last 30 Days</option>\n                <option value=\"90\"")
		val __jte_html_attribute_1 = days == 90
		if (__jte_html_attribute_1) {
		jteOutput.writeContent(" selected")
		}
		jteOutput.writeContent(">Last 90 Days</option>\n                <option value=\"180\"")
		val __jte_html_attribute_2 = (days == 180) 
		if (__jte_html_attribute_2) {
		jteOutput.writeContent(" selected")
		}
		jteOutput.writeContent(">Last 6 Months</option>\n                <option value=\"365\"")
		val __jte_html_attribute_3 = days == 365
		if (__jte_html_attribute_3) {
		jteOutput.writeContent(" selected")
		}
		jteOutput.writeContent(">Last Year</option>\n            </select>\n        </div>\n    </header>\n\n    ")
		jteOutput.writeContent("\n    <div class=\"grid grid-cols-1 md:grid-cols-3 gap-6 mt-8\">\n\n        ")
		jteOutput.writeContent("\n        <div class=\"bg-white p-6 rounded-lg shadow-md\">\n            <div class=\"flex justify-between items-start\">\n                <div>\n                    <p class=\"text-sm font-medium text-gray-500 uppercase\">Revenue</p>\n                    <p id=\"kpi-revenue\"\n                       class=\"text-3xl font-bold text-gray-800 mt-2\">")
		jteOutput.setContext("p", null)
		jteOutput.writeUserContent(periodMetricDto.totalRevenue)
		jteOutput.writeContent("</p>\n                </div>\n                <div class=\"bg-indigo-100 text-indigo-600 rounded-full p-3\">\n                    <svg class=\"w-6 h-6\" fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\"\n                         xmlns=\"http://www.w3.org/2000/svg\">\n                        <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\"\n                              d=\"M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.105 0 2 .895 2 2s-.895 2-2 2-2-.895-2-2 .895-2 2 2zm0 12c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.105 0 2 .895 2 2s-.895 2-2 2-2-.895-2-2 .895-2 2 2z\"></path>\n                    </svg>\n                </div>\n            </div>\n            <div class=\"mt-4 flex items-baseline\">\n                <span id=\"kpi-revenue-growth\" class=\"text-2xl font-semibold\"></span>\n                <span class=\"ml-2 text-sm text-gray-500\">vs. previous period</span>\n            </div>\n        </div>\n\n        ")
		jteOutput.writeContent("\n        <div class=\"bg-white p-6 rounded-lg shadow-md\">\n            <div class=\"flex justify-between items-start\">\n                <div>\n                    <p class=\"text-sm font-medium text-gray-500 uppercase\">Profit Margin</p>\n                    <p id=\"kpi-profit-margin\"\n                       class=\"text-3xl font-bold text-gray-800 mt-2\">")
		jteOutput.setContext("p", null)
		jteOutput.writeUserContent(periodMetricDto.totalCogs)
		jteOutput.writeContent("</p>\n                </div>\n                <div class=\"bg-green-100 text-green-600 rounded-full p-3\">\n                    <svg class=\"w-6 h-6\" fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\"\n                         xmlns=\"http://www.w3.org/2000/svg\">\n                        <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\"\n                              d=\"M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z\"></path>\n                    </svg>\n                </div>\n            </div>\n            <div class=\"mt-4 flex items-baseline\">\n                <span id=\"kpi-profit-margin-change\" class=\"text-2xl font-semibold\"></span>\n                <span class=\"ml-2 text-sm text-gray-500\">vs. previous period</span>\n            </div>\n        </div>\n\n        ")
		jteOutput.writeContent("\n        <div class=\"bg-white p-6 rounded-lg shadow-md\">\n            <div class=\"flex justify-between items-start\">\n                <div>\n                    <p class=\"text-sm font-medium text-gray-500 uppercase\">Transactions</p>\n                    <p id=\"kpi-transactions\"\n                       class=\"text-3xl font-bold text-gray-800 mt-2\">")
		jteOutput.setContext("p", null)
		jteOutput.writeUserContent(periodMetricDto.transactionCount)
		jteOutput.writeContent("</p>\n                </div>\n                <div class=\"bg-blue-100 text-blue-600 rounded-full p-3\">\n                    <svg class=\"w-6 h-6\" fill=\"none\" stroke=\"currentColor\" viewBox=\"0 0 24 24\"\n                         xmlns=\"http://www.w3.org/2000/svg\">\n                        <path stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\"\n                              d=\"M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z\"></path>\n                    </svg>\n                </div>\n            </div>\n            <div class=\"mt-4 flex items-baseline\">\n                <span id=\"kpi-transactions-change\" class=\"text-2xl font-semibold\"></span>\n                <span class=\"ml-2 text-sm text-gray-500\">vs. previous period</span>\n            </div>\n        </div>\n    </div>\n\n\n    <div id=\"chart-container\"\n         class=\"grid grid-cols-1 lg:grid-cols-5 gap-6 mt-8\"\n         hx-get=\"/dashboard/chart-partial?days=7\"\n         hx-trigger=\"load\"\n         style=\"width: 100%;\">\n\n        <p class=\"text-center\">Loading chart...</p>\n    </div>\n</div>\n\n</body>\n</html>\n")
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val periodMetricDto = params["periodMetricDto"] as PeriodMetricsUiDto
		val days = params["days"] as Int? ?: 30
		val groupedChartData = params["groupedChartData"] as List<GroupedChartDataUiDto>
		render(jteOutput, jteHtmlInterceptor, periodMetricDto, days, groupedChartData);
	}
}
}
