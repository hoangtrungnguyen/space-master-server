package com.space.subadmin.orders

import com.space.subadmin.common.extensions.toFormattedString
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping



@Controller
@RequestMapping("/orders")
class OrderWebController(
    private val orderRepository: OrderRepository
) {

    @GetMapping("/list")
    fun showOrderList(model: Model): String {
        model.addAttribute("orders", orderRepository.findAllWithCustomer().map {
            OderListItemDto(
                uuid = it.uuid.toString(),
                orderDate = it.orderDate.toFormattedString("yyyy-MM-dd HH:mm:ss"),
                status = it.status.toString(),
                totalAmount = it.totalAmount,
                customerName = it.customer?.fullName ?: ""
            )
        })
        return "orders/orders-list"
    }

    @GetMapping("/{id}")
    fun showOrderDetail(@PathVariable id: Long, model: Model): String {
        val orderOptional = orderRepository.findOrderDetailById(id)
        if (orderOptional.isEmpty) {
            return "redirect:/orders/list"
        }
        val order = orderOptional.get()
        val orderDetail = OrderDetail(
            id = order.id,
            orderDate = order.orderDate,
            status = order.status.toString(),
            totalAmount = order.totalAmount,
            customerName = order.customer?.fullName ?: "",
            shippingAddress = order.shippingAddress,
            billingAddress = order.billingAddress ?: "",
            items = order.items.map {
                OrderItemDetail(
                    productName = it.productVariant.product.name,
                    quantity = it.quantity,
                    pricePerUnit = it.pricePerUnit,
                    productVariantId = it.productVariant.id
                )
            }
        )
        model.addAttribute("order", orderDetail)
        return "orders/order-detail"
    }

}
