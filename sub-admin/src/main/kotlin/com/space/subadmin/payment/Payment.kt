package com.space.subadmin.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.space.subadmin.orders.Order
import com.space.subadmin.products.ProductVariant
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// --- Core Enums ---

enum class PaymentMethodType {
    CASH,
    CARD,
    BANK_TRANSFER
}

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

// --- Type-Safe Metadata Structure ---

sealed interface PaymentMethodDetails

data class CashDetails(val notes: String? = null) : PaymentMethodDetails

data class CardDetails(
    val token: String,
    val last4: String,
    val brand: String,
    val gatewayTransactionId: String
) : PaymentMethodDetails

// --- JPA Converter for Metadata ---

@Converter
class PaymentMethodDetailsConverter : AttributeConverter<PaymentMethodDetails, String> {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    override fun convertToDatabaseColumn(attribute: PaymentMethodDetails?): String? {
        if (attribute == null) return null
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): PaymentMethodDetails? {
        if (dbData == null) return null
        return try {
            objectMapper.readValue(dbData, CardDetails::class.java)
        } catch (e: Exception) {
            try {
                objectMapper.readValue(dbData, CashDetails::class.java)
            } catch (e2: Exception) {
                throw IllegalArgumentException("Could not deserialize payment details: $dbData", e2)
            }
        }
    }
}

// --- Main Payment and PaymentDetail Entities ---

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",  )
    var order: Order,

    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    var currency: Currency = Currency.USD,

    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    var paymentMethod: PaymentMethodType,

    @Convert(converter = PaymentMethodDetailsConverter::class)
    @Column(columnDefinition = "jsonb")
    var metadata: PaymentMethodDetails? = null,

    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) {

}

enum class Currency {
    USD, EUR, GBP, JPY
}
