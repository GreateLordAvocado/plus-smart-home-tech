package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new HashMap<>();

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;

    @Column(name = "delivery_weight")
    private Double deliveryWeight;

    @Column(name = "delivery_volume")
    private Double deliveryVolume;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "delivery_country")
    private String deliveryCountry;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_street")
    private String deliveryStreet;

    @Column(name = "delivery_house")
    private String deliveryHouse;

    @Column(name = "delivery_flat")
    private String deliveryFlat;
}