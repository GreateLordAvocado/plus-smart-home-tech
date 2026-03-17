package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.order.client.OrderFeignClient;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderState;
import ru.yandex.practicum.commerce.interactionapi.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.payment.dto.PaymentState;
import ru.yandex.practicum.commerce.interactionapi.store.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductDto;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repo.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderFeignClient orderFeignClient;

    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto orderDto) {
        BigDecimal total = BigDecimal.ZERO;

        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            return total;
        }

        for (Map.Entry<UUID, Long> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantityValue = entry.getValue();

            long quantity = quantityValue == null ? 0L : quantityValue;
            if (productId == null || quantity <= 0L) {
                continue;
            }

            ProductDto product = shoppingStoreClient.getProduct(productId);
            BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();

            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto orderDto) {
        BigDecimal productTotal = orderDto.getProductPrice() == null
                ? productCost(orderDto)
                : orderDto.getProductPrice();

        BigDecimal deliveryTotal = orderDto.getDeliveryPrice() == null
                ? BigDecimal.ZERO
                : orderDto.getDeliveryPrice();

        BigDecimal feeTotal = productTotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        return productTotal.add(feeTotal).add(deliveryTotal).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        BigDecimal productTotal = productCost(orderDto);
        BigDecimal deliveryTotal = orderDto.getDeliveryPrice() == null
                ? BigDecimal.ZERO
                : orderDto.getDeliveryPrice();
        BigDecimal feeTotal = productTotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayment = productTotal.add(feeTotal).add(deliveryTotal).setScale(2, RoundingMode.HALF_UP);

        PaymentEntity entity = new PaymentEntity();
        entity.setPaymentId(UUID.randomUUID());
        entity.setOrderId(orderDto.getOrderId());
        entity.setProductTotal(productTotal);
        entity.setDeliveryTotal(deliveryTotal);
        entity.setFeeTotal(feeTotal);
        entity.setTotalPayment(totalPayment);
        entity.setPaymentState(PaymentState.PENDING);

        PaymentEntity saved = paymentRepository.save(entity);

        return toDto(saved);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        PaymentEntity entity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        entity.setPaymentState(PaymentState.SUCCESS);
        paymentRepository.save(entity);

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(entity.getOrderId());
        orderDto.setPaymentId(entity.getPaymentId());
        orderDto.setState(OrderState.PAID);

        orderFeignClient.payment(orderDto);
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        PaymentEntity entity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        entity.setPaymentState(PaymentState.FAILED);
        paymentRepository.save(entity);

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(entity.getOrderId());
        orderDto.setPaymentId(entity.getPaymentId());
        orderDto.setState(OrderState.PAYMENT_FAILED);

        orderFeignClient.paymentFailed(orderDto);
    }

    private PaymentDto toDto(PaymentEntity entity) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(entity.getPaymentId());
        dto.setTotalPayment(entity.getTotalPayment());
        dto.setDeliveryTotal(entity.getDeliveryTotal());
        dto.setFeeTotal(entity.getFeeTotal());
        dto.setProductTotal(entity.getProductTotal());
        dto.setPaymentState(entity.getPaymentState());
        return dto;
    }
}