package com.space.subadmin.orders

import com.space.subadmin.db.Order
import com.space.subadmin.db.OrderStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/dev/orders")
class DevOrderController(private val devOrderRepository: DevOrderRepository) {

    @PostMapping("/create-test")
    fun createTestOrder(): Order {
        val testOrder = Order(
            shippingAddress = "123 Test Street",
            status = OrderStatus.PENDING,
            totalAmount = BigDecimal("99.99")
        )
        return devOrderRepository.save(testOrder)
    }
}
