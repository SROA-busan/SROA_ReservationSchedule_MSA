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
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userNum;

    private String id;
    private String pw;
    private String name;
    private String address;
    private String phoneNum;
    private Integer code;

    @Builder
    public UserInfo(String id, String pw, String name, String address, String phoneNum) {
        this.id = id;
        this.pw = pw;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
    }


}
