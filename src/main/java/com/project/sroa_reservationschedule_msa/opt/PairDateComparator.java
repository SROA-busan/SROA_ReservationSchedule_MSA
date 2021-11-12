package com.project.sroa_reservationschedule_msa.opt;

import java.util.Comparator;

public class PairDateComparator implements Comparator<Pair> {
    @Override
    public int compare(Pair pair1, Pair pair2) {
        return pair1.date.compareTo(pair2.date);
    }
}
