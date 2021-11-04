package com.project.sroa_reservationschedule_msa.service;

import com.project.sroa_reservationschedule_msa.model.*;
import com.project.sroa_reservationschedule_msa.opt.SortElem;
import com.project.sroa_reservationschedule_msa.repository.EngineerInfoRepository;
import com.project.sroa_reservationschedule_msa.repository.ProductRepository;
import com.project.sroa_reservationschedule_msa.repository.ScheduleRepository;
import com.project.sroa_reservationschedule_msa.repository.UserInfoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OptimizationServiceImpl implements OptimizationService {
    String[] times = {"09:00", "10:30", "12:30", "14:00", "15:30", "17:00"};
    EngineerInfoRepository engineerInfoRepository;
    ProductRepository productRepository;
    ScheduleRepository scheduleRepository;
    UserInfoRepository userInfoRepository;

    public OptimizationServiceImpl(EngineerInfoRepository engineerInfoRepository,
                                   ProductRepository productRepository,
                                   ScheduleRepository scheduleRepository,
                                   UserInfoRepository userInfoRepository) {
        this.engineerInfoRepository = engineerInfoRepository;
        this.productRepository = productRepository;
        this.scheduleRepository = scheduleRepository;
        this.userInfoRepository = userInfoRepository;
    }


    //날짜와 가장 가까운 서비스 센터가 주어졌을때 시간대 마다 가능한 엔지니어가 있는지를 조회
    @Override
    public List<Boolean> searchAvailableTime(String date, Map<String, Object> closeCenter) {
        // 가까운 서비스 센텀 찾기
        ServiceCenter serviceCenter = (ServiceCenter) closeCenter.get("center");

        // 해당 센터의 엔지니어의 일정 조회
        List<Boolean> res = new ArrayList<>();
        Map<String, Object> availableEngineers = noScheduleEngineerAtTime(date, serviceCenter);

        for (String time : times) {
            time = date + " " + time;
            Integer possibleEngineersAtTimeCnt = ((List<EngineerInfo>) availableEngineers.get(time)).size();

            if (possibleEngineersAtTimeCnt > 0) {
                res.add(true);
            } else {
                res.add(false);
            }
            System.out.println(date + " " + time + "에 예약가능한 엔지니어 수 : " + possibleEngineersAtTimeCnt);
        }
        System.out.println("사용 가능 시간 대");
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i) == true) System.out.println(times[i]);
        }
        System.out.println("=================================================");
        return res;
    }

    // 날짜 선택시 가능한 엔지니어, 날짜 + 시간 선택시 엔지니어 할당을 위한 가능 엔지니어
    // return map 시간대 - 가능 엔지니어 리스트
    @Override
    public Map<String, Object> noScheduleEngineerAtTime(String date, ServiceCenter serviceCenter) {
        Map<String, Object> map = new HashMap<>();

        // 변수로 날짜만 들어오면 날짜 + 6개의 시간으로 탐색
        // 변수로 날짜 + 시간이 들어오면 해당 날짜 + 시간에 대해 탐색
        String[] searchTimes;
        if (date.length() < 11) {
            searchTimes = new String[6];
            for (int i = 0; i < searchTimes.length; i++) {
                searchTimes[i] = date + " " + times[i];
                System.out.println(searchTimes[i]);
            }
        } else {
            searchTimes = new String[]{date};
        }
        System.out.println("=================================================");
        System.out.println("담당 서비스 센터 번호 : " + serviceCenter.getCenterNum());

        // 시간별 탐색 가능한 엔지니어 리스트
        for (String time : searchTimes) {
            String dateTime = time;

            List<EngineerInfo> list = engineerInfoRepository.findAllPossibleEngineerByDate(serviceCenter.getCenterNum(), dateTime);
            System.out.println("탐색 시간대 " + dateTime + "에 가능한 엔지니어 수 : " + list.size());
            map.put(time, list);
        }
        System.out.println("=================================================");
        return map;
    }

    @Override
    public List<Long> findOptimunEngineers(List<SortElem> decideList) {
        List<Long> sortEngineerNumList = new ArrayList<>();
        System.out.println("현재 그룹내 엔지니어 수 " + decideList.size());
        Integer distMean = calcMean(decideList);
        System.out.println("엔지니어와 고객 거리의 평균 : " + distMean);
        double distDev = calcDev(decideList);
        System.out.println("엔지니어와 고객 거리의 표준편차 : " + distDev);
        System.out.println("-------------------------------------------");

        if (decideList.size() == 1) {
            sortEngineerNumList.add(decideList.get(0).getNum());
            return sortEngineerNumList;
        }

        // 비교적 거리가 먼 후보 제외
        if (distDev != 0.0) {
            for (int i = 0; i < decideList.size(); i++) {
                if (decideList.get(i).getDist() >= distMean + distDev) {
                    System.out.println("고객과 거리가 너무 멀어 배제되는 엔지니어 : " + decideList.get(i).getNum() + ", 거리 : " + decideList.get(i).getDist());
                    decideList.remove(i);
                }
            }
        }

        System.out.println("거리상 배제후 남은 엔지니어의 수: " + decideList.size());
        List<SortElem> sortList = new ArrayList<>();

        Integer minDist = 987654321;
        Integer minDirDiff = 987;

        for (int i = 0; i < decideList.size(); i++) {
            if (decideList.get(i).getDist() < distMean - distDev) {
                System.out.println("거리가 너무 가까워 선별 가능성이 높은 엔지니어 : " + decideList.get(i).getNum() + ", 거리 : " + decideList.get(i).getDist());

                sortList.add(decideList.get(i));
            }
        }
        System.out.println("-------------------------------------------");

        // 현재 그룹내에서 거리와 방향성 차이가 최소인 엔지니어 선별
        // 거리 유독 가까운 엔지니어 중에서 선발
        if (sortList.size() > 0) {
            System.out.println("거리가 너무 가까운 엔지니어 존재 ");
            for (int i = 0; i < sortList.size(); i++) {
                System.out.println(decideList.get(i).getNum() + "의 거리 : " + sortList.get(i).getDist());
                if (minDist > sortList.get(i).getDist()) {
                    minDist = sortList.get(i).getDist();
                }
            }
            System.out.println("가장 짧은 거리 : " + minDist);
            for (int i = 0; i < sortList.size(); i++) {
                if (minDist == sortList.get(i).getDist()) {
                    sortEngineerNumList.add(sortList.get(i).getNum());
                }
            }
        }
        //거리가 비슷하여 방향성으로 선별
        // 방향성 차이가 가장 적은 엔지니어 선별
        else {
            System.out.println("거리가 비슷하여 방향성 차이로 선별");
            for (int i = 0; i < decideList.size(); i++) {
                System.out.println(decideList.get(i).getNum() + "의 방향성 : " + decideList.get(i).getDirDiff());
                if (minDirDiff > decideList.get(i).getDirDiff()) {
                    minDirDiff = decideList.get(i).getDirDiff();
                }
            }
            System.out.println("가장 작은 방향성 : " + minDirDiff);
            for (int i = 0; i < decideList.size(); i++) {
                if (minDirDiff == decideList.get(i).getDirDiff())
                    sortEngineerNumList.add(decideList.get(i).getNum());
            }
        }
        return sortEngineerNumList;
    }

    @Override
    public EngineerInfo findSmallestWorkEngineerAmongOptimum(List<Long> sortEngineerNumList) {
        System.out.println("최종 선별 후 엔지니어 수 : " + sortEngineerNumList.size());
        EngineerInfo res = null;

        // 결과 엔지니어가 여러명이라면 작업량으로 선별
        if (sortEngineerNumList.size() > 1) {
            System.out.println("최종 선별된 엔지니어가 여러명 -> " + sortEngineerNumList.size() + "명 -> 작업량으로 결정");
            int minIdx = 0;
            int min = engineerInfoRepository.findWorkByNum(sortEngineerNumList.get(0));
            for (int i = 1; i < sortEngineerNumList.size(); i++) {
                int temp = engineerInfoRepository.findWorkByNum(sortEngineerNumList.get(i));
                System.out.println(sortEngineerNumList.get(i) + "의 작업량 : " + temp);
                if (min >= temp) {
                    min = temp;
                    minIdx = i;
                }
            }
            res = engineerInfoRepository.findByEngineerNum(sortEngineerNumList.get(minIdx));
        }
        // 결과 1명 -> 엔지니어 배정
        else {
            res = engineerInfoRepository.findByEngineerNum(sortEngineerNumList.get(0));
        }
        return res;
    }

    @Override
    public Product storeProductForReserve(String classifyName, String content) {
        Product product = Product.builder()
                .classifyName(classifyName)
                .problem(content)
                .build();
        return productRepository.save(product);
    }

    @Override
    public void allocateSchedule(EngineerInfo engineerInfo, Product product,
                                 String dateTime, String userId,
                                 String customerName, String phoneNum, String address) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        UserInfo userInfo = userInfoRepository.findByuserId(userId);
        Schedule schedule = Schedule.builder()
                .product(product)
                .engineerInfo(engineerInfo)
                .startDate(LocalDateTime.parse(dateTime, formatter))
                .customerName(customerName)
                .phoneNum(phoneNum)
                .address(address)
                .userInfo(userInfo)
                .build();
        scheduleRepository.save(schedule);
        engineerInfoRepository.updateEngineerAmountOfWork(engineerInfo.getEngineerNum());
    }

    // 반납 서비스

    @Override
    public Schedule findScheduleByScheduleNum(Long scheduleNum) {
        return scheduleRepository.findByScheduleNum(scheduleNum);
    }

    @Override
    public EngineerInfo findEngineerByScheduleNum(Long scheduleNum) {
        return scheduleRepository.findByScheduleNum(scheduleNum).getEngineerInfo();
    }

    @Override
    public List<Boolean> searchAvailableTimeForReturn(Long engineerNum, String date) {

        List<Boolean> res = new ArrayList<>();
        for (String time : times) {
            time = date + " " + time;
            System.out.println(time);
            List<Schedule> schedules=scheduleRepository.findAllScheduleTimeByEngineerNumAndDate(engineerNum, time);
            if (schedules.size() == 0)
                res.add(true);
            else
                res.add(false);
        }
        return res;
    }

    @Override
    public void allocateReturnSchedule(Long scheduleNum, String dateTime) {
        System.out.println(dateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime endDate = LocalDateTime.parse(dateTime, formatter);
        System.out.println(endDate);
        scheduleRepository.updateForReturn(scheduleNum, endDate);
    }

    // 평균구하기
    private Integer calcMean(List<SortElem> decideList) {
        Integer sum = 0;
        for (int i = 0; i < decideList.size(); i++) {
            sum += decideList.get(i).getDist();
        }
        return (int) sum / decideList.size();
    }

    // 표준편차 구하기
    private Double calcDev(List<SortElem> decideList) {
        double sum = 0.0;
        double sd = 0.0;
        double diff;
        double meanValue = calcMean(decideList);
        for (int i = 0; i < decideList.size(); i++) {
            diff = decideList.get(i).getDist() - meanValue;
            sum += diff * diff;
        }
        return Math.sqrt(sum / decideList.size());
    }
}
