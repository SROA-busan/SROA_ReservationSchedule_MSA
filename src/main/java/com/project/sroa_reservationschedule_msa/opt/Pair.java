package com.project.sroa_reservationschedule_msa.opt;

import lombok.Getter;

import java.util.Comparator;

@Getter
public class Pair {
    Long num;
    String date;

    public Pair(Long num, String date) {
        this.num = num;
        this.date = date;
    }



}
