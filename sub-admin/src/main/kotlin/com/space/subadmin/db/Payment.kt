package com.space.subadmin.db

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.space.subadmin.users.SnowflakeIdSequence
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

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

sealed interface PaymentMethodDetails

data class CashDetails(val notes: String? = null) : PaymentMethodDetails

data class CardDetails(
    val token: String,
    val last4: String,
    val brand: String,
    val gatewayTransactionId: String
) : PaymentMethodDetails

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

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @SnowflakeIdSequence
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var uuid: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",  )
    var order: Order,

    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    var currency: Currency,

    @Enumerated(EnumType.STRING)
    var status: PaymentStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    var paymentMethod: PaymentMethodType,

    @Convert(converter = PaymentMethodDetailsConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val metadata: PaymentMethodDetails? = null,

     late i  nitvar createdAt: Instant?,
     var updatedAt: Instant?,
) {

    @PrePersist
    fun onPrePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onPreUpdate(){
        updatedAt = Instant.now()
    }

}

enum class Currency {
    USD, EUR, GBP, JPY
}