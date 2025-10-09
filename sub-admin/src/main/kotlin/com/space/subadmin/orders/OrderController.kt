package com.space.subadmin.orders

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/orders")
class OrderWebController(
    private val orderRepository: OrderRepository
) {

    @GetMapping("/list")
    fun showOrderList(model: Model): String {
        model.addAttribute("orders", orderRepository.findAll())
        return "orders-list"
    }

}