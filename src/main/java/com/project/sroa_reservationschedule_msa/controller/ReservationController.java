package com.project.sroa_reservationschedule_msa.controller;

import com.project.sroa_reservationschedule_msa.dto.RequestBooking;
import com.project.sroa_reservationschedule_msa.model.EngineerInfo;
import com.project.sroa_reservationschedule_msa.model.Product;
import com.project.sroa_reservationschedule_msa.model.ServiceCenter;
import com.project.sroa_reservationschedule_msa.opt.Coordinates;
import com.project.sroa_reservationschedule_msa.opt.SortElem;
import com.project.sroa_reservationschedule_msa.service.LocationService;
import com.project.sroa_reservationschedule_msa.service.OptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
public class ReservationController {
    LocationService locationService;
    OptimizationService optimizationService;

    @Autowired
    public ReservationController(LocationService locationService,
                                 OptimizationService optimizationService) {
        this.locationService = locationService;
        this.optimizationService = optimizationService;
    }

    // 고객 날짜 선택시 예약 가능 현황 조회
    //[ 09:00 ,10:30, 12:30, 14:00, 15:30, 17:00]
    @GetMapping("/schedule/findAvailableTime/{date}/{address}")
    public List<Boolean> findAvailableTime(@PathVariable("date") String date,
                                           @PathVariable("address") String address) {
        //고객 주소와 가까운 서비스 센터 찾기
        Map<String, Object> closeCenter = locationService.searchNearCenter(address);

        return optimizationService.searchAvailableTime(date, closeCenter);
    }

    @PostMapping("/schedule/allocateEngineer")
    public EngineerInfo allocateEngineer(@RequestBody RequestBooking form) {
        //고객 주소와 가까운 서비스 센터와 거리 찾기
        Map<String, Object> closeCenter = locationService.searchNearCenter(form.getAddress());

        // 고객이 기입한 날짜 +  시간에 일정이 없는 엔지니어 조회
        Map<String, Object> noScheduleEngineers = optimizationService.noScheduleEngineerAtTime(form.getDateTime(), (ServiceCenter) closeCenter.get("center"));
        List<EngineerInfo> engineers = (List<EngineerInfo>) noScheduleEngineers.get(form.getDateTime());

        Integer distBetCenterAndCust = (Integer) closeCenter.get("distance");
        Integer dirFromCenter = (Integer) closeCenter.get("dir");

        List<SortElem> infoForOptimum = locationService.findInfoForOptimum(engineers,
                distBetCenterAndCust, dirFromCenter, form.getDateTime(),
                (Coordinates) closeCenter.get("centerCoor"),
                (Coordinates) closeCenter.get("customerCoor"));

        List<Long> sortEngineerNumList = optimizationService.findOptimunEngineers(infoForOptimum);

        // 선별된 엔지니어가 여러명일수 있기때문에 작업량으로 최종 선별
        EngineerInfo engineerInfo = optimizationService.findSmallestWorkEngineerAmongOptimum(sortEngineerNumList);

        //해당 엔지니어에 일정 부여
        Product product = optimizationService.storeProductForReserve(form.getClassifyName(), form.getContent());
        optimizationService.allocateSchedule(engineerInfo, product, form.getDateTime(), form.getUserNum(), form.getCustomerName(), form.getPhoneNum(), form.getAddress());
        return engineerInfo;
    }

}
