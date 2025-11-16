package com.space.subadmin.db

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CashDetails::class, name = "cash"),
    JsonSubTypes.Type(value = CardDetails::class, name = "card")
)
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
        if (dbData.isNullOrBlank()) return null
        return try {
            // Try new format first (with "type" property)
            objectMapper.readValue(dbData, PaymentMethodDetails::class.java)
        } catch (e: Exception) {
            // Fallback to old format (guessing)
            try {
                objectMapper.readValue(dbData, CardDetails::class.java)
            } catch (e2: Exception) {
                try {
                    objectMapper.readValue(dbData, CashDetails::class.java)
                } catch (e3: Exception) {
                    throw IllegalArgumentException("Could not deserialize payment details in either new or old format: $dbData", e3)
                }
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

    val metadata: String? = null,
) {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    lateinit var order: Order

    lateinit var amount: BigDecimal

    @Enumerated(EnumType.STRING)
    lateinit var currency: Currency

    @Enumerated(EnumType.STRING)
    lateinit var status: PaymentStatus

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    lateinit var paymentMethod: PaymentMethodType

    lateinit var createdAt: Instant

    lateinit var updatedAt: Instant

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
    USD, EUR, GBP, JPY, VND
}