package com.carrental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CarType type;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    protected Car() {
    }

    public Car(UUID id, CarType type, String brand, String model, String photoUrl) {
        this.id = id;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.photoUrl = photoUrl;
    }

    public UUID getId() {
        return id;
    }

    public CarType getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
