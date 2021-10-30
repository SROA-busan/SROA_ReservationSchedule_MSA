package com.project.sroa_reservationschedule_msa.service;

import com.project.sroa_reservationschedule_msa.model.EngineerInfo;
import com.project.sroa_reservationschedule_msa.model.Product;
import com.project.sroa_reservationschedule_msa.model.ServiceCenter;
import com.project.sroa_reservationschedule_msa.opt.SortElem;

import java.util.List;
import java.util.Map;

public interface OptimizationService {
    List<Boolean> searchAvailableTime(String date, Map<String, Object> closeCenter);

    Map<String, Object> noScheduleEngineerAtTime(String date, ServiceCenter serviceCenter);

    EngineerInfo findSmallestWorkEngineerAmongOptimum(List<Long> sortEngineerNumList);

    Product storeProductForReserve(String classifyName, String content);

    void allocateSchedule(EngineerInfo engineerInfo, Product product, String dateTime, Long userNum, String customerName, String phoneNum, String address);

    List<Long> findOptimunEngineers(List<SortElem> infoForOptimum);
}
