package com.space.subadmin.payment

import com.space.subadmin.db.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
}

