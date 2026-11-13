package com.carrental.repository;

import com.carrental.model.Car;
import com.carrental.model.CarType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    List<Car> findAllByOrderByTypeAscBrandAscModelAsc();

    List<Car> findByTypeOrderByBrandAscModelAsc(CarType type);
}
