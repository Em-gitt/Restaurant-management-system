package com.unipoli.kitchen.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kitchen_order_items")
public class KitchenOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kitchen_order_id", nullable = false)
    private KitchenOrder kitchenOrder;

    @Column(name = "dish_id", nullable = false)
    private Long dishId;

    @Column(name = "dish_name", nullable = false, length = 150)
    private String dishName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "special_instructions", length = 255)
    private String specialInstructions;

    public KitchenOrderItem() {
    }

    public KitchenOrderItem(Long dishId, String dishName, Integer quantity, String specialInstructions) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KitchenOrder getKitchenOrder() {
        return kitchenOrder;
    }

    public void setKitchenOrder(KitchenOrder kitchenOrder) {
        this.kitchenOrder = kitchenOrder;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
}
