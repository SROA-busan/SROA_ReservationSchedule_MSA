package com.project.sroa_reservationschedule_msa.repository;

import com.project.sroa_reservationschedule_msa.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll();

    @Query(nativeQuery = true, value = "SELECT p.* FROM product p WHERE p.product_num = (SELECT s.product_num FROM Schedule s WHERE s.schedule_num=?1)")
    Product findByScheduleNum(Long scheduleNum);
}
