package com.project.sroa_reservationschedule_msa.service;

import com.project.sroa_reservationschedule_msa.model.EngineerInfo;
import com.project.sroa_reservationschedule_msa.opt.Coordinates;
import com.project.sroa_reservationschedule_msa.opt.SortElem;

import java.util.List;
import java.util.Map;

public interface LocationService {
    Map<String, Object> searchNearCenter(String address);
    List<SortElem> findInfoForOptimum(List<EngineerInfo> engineers, Integer distBetweenCustomerAndCenter, Integer dirFromCenter, String dateTime, Coordinates centerCoor, Coordinates customerCoor);



}
