package com.project.sroa_reservationschedule_msa.model;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class EmployeeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long empNum;
    public String name;

}
