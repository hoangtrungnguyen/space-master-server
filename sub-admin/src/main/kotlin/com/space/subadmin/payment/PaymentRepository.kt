package com.space.subadmin.payment

import com.space.subadmin.db.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findOneByUuid(uuid: UUID): Payment?
}

