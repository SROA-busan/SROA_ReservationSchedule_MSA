package com.project.sroa_reservationschedule_msa.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class EngineerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long engineerNum;
    private Integer avgScore;
    private Integer amountOfWork;


    @ManyToOne
    @JoinColumn(name = "centerNum")
    private ServiceCenter serviceCenter;

    @OneToOne
    @JoinColumn(name = "userNum")
    private UserInfo userInfo;

    @OneToOne
    @JoinColumn(name = "empNum")
    private EmployeeInfo employeeInfo;

    @Builder
    public EngineerInfo(UserInfo userInfo, EmployeeInfo employeeInfo, ServiceCenter serviceCenter) {
        this.serviceCenter = serviceCenter;
        this.avgScore = 0;
        this.amountOfWork = 0;
        this.userInfo = userInfo;
        this.employeeInfo = employeeInfo;
    }
}
