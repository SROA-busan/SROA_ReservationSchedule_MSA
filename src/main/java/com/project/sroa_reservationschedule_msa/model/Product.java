package com.project.sroa_reservationschedule_msa.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productNum;
    private String classifyName;
    private String problem;

    @Builder
    public Product(String classifyName, String problem){
        this.classifyName=classifyName;
        this.problem=problem;
    }
}
