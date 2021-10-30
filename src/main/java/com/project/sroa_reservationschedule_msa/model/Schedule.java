package com.project.sroa_reservationschedule_msa.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long scheduleNum;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer status;
    private String address;
    private String customerName;
    private String phoneNum;

    @OneToOne
    @JoinColumn(name = "productNum")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "userNum")
    private UserInfo userInfo;

    @ManyToOne
    @JoinColumn(name = "engineerNum")
    private EngineerInfo engineerInfo;

    @Builder
    public Schedule(LocalDateTime startDate, String address, String customerName,
                    String phoneNum, Product product, UserInfo userInfo, EngineerInfo engineerInfo){
        this.status=0;
        this.startDate=startDate;
        this.address=address;
        this.customerName=customerName;
        this.phoneNum=phoneNum;
        this.product=product;
        this.userInfo=userInfo;
        this.engineerInfo=engineerInfo;
    }
}
