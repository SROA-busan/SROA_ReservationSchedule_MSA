package com.project.sroa_reservationschedule_msa.repository;

import com.project.sroa_reservationschedule_msa.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll();


}
