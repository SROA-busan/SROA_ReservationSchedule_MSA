package com.project.sroa_reservationschedule_msa.opt;

import lombok.Getter;

@Getter
public class Coordinates {
    Double lon; //경도
    Double lat; //위도

    public Coordinates(Double x, Double y) {
        this.lon = x;
        this.lat = y;
    }
}