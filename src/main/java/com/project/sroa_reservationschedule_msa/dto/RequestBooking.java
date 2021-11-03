package com.project.sroa_reservationschedule_msa.dto;

import lombok.Getter;

@Getter
public class RequestBooking {
    private String userId;
    private String customerName;
    private String classifyName;
    private String address;
    private String dateTime;
    private String phoneNum;
    private String content;
}
