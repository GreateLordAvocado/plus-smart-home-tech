package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.interactionapi.payment.dto.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Column(name = "delivery_total", nullable = false)
    private BigDecimal deliveryTotal;

    @Column(name = "fee_total", nullable = false)
    private BigDecimal feeTotal;

    @Column(name = "product_total", nullable = false)
    private BigDecimal productTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state", nullable = false)
    private PaymentState paymentState;
}