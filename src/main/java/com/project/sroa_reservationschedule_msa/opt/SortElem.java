package com.project.sroa_reservationschedule_msa.opt;

import lombok.Getter;

@Getter
public class SortElem {
        Long num;
        Integer dist;
        Integer dirDiff;

        public SortElem(Long num, Integer dist, Integer dirDiff) {
            this.num = num;
            this.dist = dist;
            this.dirDiff = dirDiff;
        }
}
