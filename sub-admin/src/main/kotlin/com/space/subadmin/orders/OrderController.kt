package com.space.subadmin.orders

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/orders")
class OrderWebController(
    private val SQLiteOrderRepository: SQLiteOrderRepository
) {

    @GetMapping("/list")
    fun showOrderList(model: Model): String {
        model.addAttribute("orders", SQLiteOrderRepository.findAllWithCustomer())
        return "orders-list"
    }

}

