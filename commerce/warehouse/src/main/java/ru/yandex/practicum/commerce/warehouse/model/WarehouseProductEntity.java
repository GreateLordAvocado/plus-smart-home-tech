package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
public class WarehouseProductEntity {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private boolean fragile;

    @Column(nullable = false)
    private double width;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false)
    private double depth;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private long quantity;

    public WarehouseProductEntity() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public boolean isFragile() {
        return fragile;
    }

    public void setFragile(boolean fragile) {
        this.fragile = fragile;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}