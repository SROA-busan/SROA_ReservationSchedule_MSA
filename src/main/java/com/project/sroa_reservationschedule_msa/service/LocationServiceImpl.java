package com.project.sroa_reservationschedule_msa.service;

import com.project.sroa_reservationschedule_msa.model.EngineerInfo;
import com.project.sroa_reservationschedule_msa.model.Schedule;
import com.project.sroa_reservationschedule_msa.model.ServiceCenter;
import com.project.sroa_reservationschedule_msa.opt.Coordinates;
import com.project.sroa_reservationschedule_msa.opt.SortElem;
import com.project.sroa_reservationschedule_msa.repository.ScheduleRepository;
import com.project.sroa_reservationschedule_msa.repository.ServiceCenterRepository;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationServiceImpl implements LocationService {
    String apiKey = "553DD31F-7E58-3853-8B42-951509B85AAF";
    Integer MAX = 987654321;



    ServiceCenterRepository serviceCenterRepository;
    ScheduleRepository scheduleRepository;

    public LocationServiceImpl(ServiceCenterRepository serviceCenterRepository,
                               ScheduleRepository scheduleRepository){
        this.serviceCenterRepository=serviceCenterRepository;
        this.scheduleRepository=scheduleRepository;
    }

    @Override
    public Map<String, Object> searchNearCenter(String customerAddress) {
        String rootAddress = customerAddress.split(" ")[0];
        List<ServiceCenter> serviceCenters = serviceCenterRepository.findByAddressContaining(rootAddress);

        Coordinates customerCoordinates = findCoordinates(customerAddress);

        Integer min = MAX;
        int min_idx = 0, idx = 0;
        for (ServiceCenter s : serviceCenters) {
            assert customerCoordinates != null;
            if (s.getLongitude() == null || s.getLatitude() == null) {
                System.out.println("현재 서비스 센터의 위도, 경도 정보가 없어 갱신");
                Coordinates coordinates = findCoordinates(s.getAddress());
                s.setLatitude(coordinates.getLat());
                s.setLongitude(coordinates.getLon());
                serviceCenterRepository.updatePos(s.getLatitude(), s.getLongitude(), s.getCenterNum());
            }

            Integer now = harverSine(customerCoordinates, new Coordinates(s.getLongitude(), s.getLatitude()));
            if (now < min) {
                min = now;
                min_idx = idx;
            }
            idx += 1;
        }
        System.out.println("=================================================");
        System.out.println("같은지역의 서비스 센터 및 최소 거리 탐색 완료");
        System.out.println("가까운 서비스 센터명 : " + serviceCenters.get(min_idx));
        System.out.println("가까운 서비스 센터주소 : " + serviceCenters.get(min_idx).getAddress());
        System.out.println("거리 : " + min + " meter");

        Map<String, Object> map = new HashMap<>();
        map.put("center", serviceCenters.get(min_idx));
        map.put("centerCoor", new Coordinates(serviceCenters.get(min_idx).getLatitude(), serviceCenters.get(min_idx).getLongitude()));
        Coordinates center = new Coordinates(serviceCenters.get(min_idx).getLatitude(), serviceCenters.get(min_idx).getLongitude());
        map.put("dir", calcDir(center, customerCoordinates));
        map.put("distance", min);
        map.put("customerCoor", customerCoordinates);
        return map;
    }

    @Override
    public List<SortElem> findInfoForOptimum(List<EngineerInfo> engineers, Integer distBetCenterAndCust, Integer dirFromCenter, String dateTime, Coordinates centerCoor, Coordinates customerCoor) {
        List<SortElem> decideList = new ArrayList<>();

//        Integer distBetweenCustomerAndCenter_first = harverSine(centerCoor, customerCoor);
        Integer dist;
        Integer dirDiff;
        Coordinates beforeCoor;
        Coordinates afterCoor;

        // 활동가능한 엔지니어의 고객과의 거리 조회
        for (EngineerInfo engineer : engineers) {
            String _dateTime = dateTime.split(" ")[0];
            List<Schedule> timeOfSchedules = scheduleRepository.findAllScheduleTimeByEngineerNumAndDate(engineer.getEngineerNum(), _dateTime);
            int scheduleSize = timeOfSchedules.size();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");
            System.out.println("현재 엔지니어 번호 : " + engineer.getEngineerNum());

            // 당일에 일정이 없으면 센터의 위치로 거리등록
            if (timeOfSchedules.size() == 0) {
                dist = distBetCenterAndCust;
                dirDiff = dirFromCenter;
                if (dirDiff >= 0 && dirDiff <= 90) {
                    dirDiff = Math.min(dirDiff, 90 - dirDiff);
                } else if (dirDiff > 90 && dirDiff <= 180) {
                    dirDiff = Math.min(dirDiff - 90, 180 - dirDiff);
                } else if (dirDiff > 180 && dirDiff <= 270) {
                    dirDiff = Math.min(dirDiff - 180, 270 - dirDiff);
                } else {
                    dirDiff = Math.min(dirDiff - 270, 360 - dirDiff);
                }
                decideList.add(new SortElem(engineer.getEngineerNum(), dist, dirDiff));
                System.out.println("당일 일정 없음");
            } else {
                Schedule lastSchedule = timeOfSchedules.get(scheduleSize - 1);
                String string = new String();
                // 마지막 일정이 고객과 만나기 전이면
                if (lastSchedule.getStatus() == 0) {
                    string = lastSchedule.getStartDate().format(formatter);
                }
                // 마지막 일정이 수령후 수리 완료 상태면
                else if (lastSchedule.getStatus() == 3) {
                    string = lastSchedule.getEndDate().format(formatter);
                }

                // 이번에 할당될 일정이 마지막 일정보다 시간적으로 뒤
                if (dateTime.compareTo(string) > 0) {
                    System.out.println("현재 할당되는 일정이 마지막 일정");
                    afterCoor = findCoordinates(timeOfSchedules.get(scheduleSize - 1).getAddress());
                    dist = harverSine(customerCoor, afterCoor);

                    // 일정이 한개라면 방향성 : 센터 - 원래 일정 - 현재 할당 일정
                    if (timeOfSchedules.size() == 1) {
                        beforeCoor = centerCoor;
                    } else {
                        beforeCoor = findCoordinates(timeOfSchedules.get(scheduleSize - 2).getAddress());
                    }
                    dirDiff = calcDirDiff(beforeCoor, afterCoor, customerCoor);

                    decideList.add(new SortElem(engineer.getEngineerNum(), dist, dirDiff));
                }
                // 현재 할당될 일정이 일정 사이 or 다른 일정들 보다 일찍 시작되는 일정정
                else {
                    int idx = 0;
                    // 할당 될 일정이 다른 일정보다 빠른지 판단 요소
                    for (Schedule schedule : timeOfSchedules) {
                        // 마지막 일정이 고객과 만나기 전이면
                        if (lastSchedule.getStatus() == 0) {
                            string = lastSchedule.getStartDate().format(formatter);
                        }
                        // 마지막 일정이 수령후 수리 완료 상태면
                        else if (lastSchedule.getStatus() == 3) {
                            string = lastSchedule.getEndDate().format(formatter);
                        }
                        if (dateTime.compareTo(string) < 0) {
                            idx -= 1;
                            break;
                        }
                        idx++;
                    }
                    // 다른 일정들보다 시간이 빠름
                    if (idx == -1) {
                        //전 - 센터, 중간 - 이번 일정, 후 - 이번에 배정되는 일정 뒤의 일정(idx+1)
                        afterCoor = findCoordinates(timeOfSchedules.get(idx + 1).getAddress());
                        dist = harverSine(customerCoor, afterCoor);

                        dirDiff = calcDirDiff(centerCoor, customerCoor, afterCoor);
                        decideList.add(new SortElem(engineer.getEngineerNum(), dist, dirDiff));
                    } else {
                        // 전 -  이번에 배정되는 일정 전의 일정(idx) 중간 - 이번 일정, 후 - 이번에 배정되는 일정 뒤의 일정(idx+1)
                        beforeCoor = findCoordinates(timeOfSchedules.get(idx).getAddress());
                        afterCoor = findCoordinates(timeOfSchedules.get(idx + 1).getAddress());
                        dist = harverSine(customerCoor, afterCoor);

                        dirDiff = calcDirDiff(beforeCoor, customerCoor, afterCoor);
                        decideList.add(new SortElem(engineer.getEngineerNum(), dist, dirDiff));
                    }
                }
            }

            System.out.println("고객과의 평가 거리 : " + dist);
        }
        System.out.println("---------------------------------------");
        // 엔지너어들의 거리, 방향성을 이용하여 1차 선별
        return decideList;
    }




    // 도로명 주소에 대해 좌표 계산
    private Coordinates findCoordinates(String customerAddress) {
        String apiURL = "http://api.vworld.kr/req/address";
        JsonParser jsonParser = JsonParserFactory.getJsonParser();
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            String text_content = URLEncoder.encode(customerAddress, StandardCharsets.UTF_8);

            String postParams = "service=address";
            postParams += "&request=getcoord";
            postParams += "&version=2.0";
            postParams += "&crs=epsg:4326";
            postParams += "&address=" + text_content;
            postParams += "&refine=true";
            postParams += "&simple=false";
            postParams += "&format=json";
            postParams += "&type=ROAD";
            postParams += "&key=" + apiKey;

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) {// 정상호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {// 에러발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            con.disconnect();
            Map<String, Object> map = jsonParser.parseMap(response.toString());
            Map<String, Object> point = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) map.get("response")).get("result")).get("point");

            return new Coordinates(Double.parseDouble((String) point.get("x")), Double.parseDouble((String) point.get("y")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 좌표간 방향성(방위각) 구구하기
    private Integer calcDir(Coordinates a, Coordinates b) {
        double lat1_rad = convertDegreesToRadians(a.getLat());
        double lat2_rad = convertDegreesToRadians(b.getLon());
        double lon_diff_rad = convertDegreesToRadians(a.getLon() - b.getLon());
        double y = Math.sin(lon_diff_rad) * Math.cos(lat2_rad);
        double x = Math.cos(lat1_rad) * Math.sin(lat2_rad) - Math.sin(lat1_rad) * Math.cos(lat2_rad) * Math.cos(lon_diff_rad);

        return ((int) convertRadiansToDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    // 방위각 차이 구하기
    Integer calcDirDiff(Coordinates before, Coordinates now, Coordinates after) {
        Integer dir1 = calcDir(before, now);
        Integer dir2 = calcDir(now, after);
        Integer diff1 = Math.abs(dir1 - dir2);
        return Math.min(diff1, 360 - diff1);
    }

    // 직선거리 미터 반환
    private Integer harverSine(Coordinates coordinates1, Coordinates coordinates2) {
        double dist;
        double radius = 6371;//지구 반지름

        double deltaLat = convertDegreesToRadians(Math.abs(coordinates1.getLat() - coordinates2.getLat()));
        double deltaLon = convertDegreesToRadians(Math.abs(coordinates1.getLon() - coordinates2.getLon()));

        double sinDeltaLat = Math.sin(deltaLat / 2);
        double sinDeltaLon = Math.sin(deltaLon / 2);

        double squareRoot = Math.sqrt(
                sinDeltaLat * sinDeltaLat +
                        Math.cos(coordinates1.getLat()) * Math.cos(coordinates2.getLat()) * sinDeltaLat * sinDeltaLon);

        dist = 2 * radius * Math.asin(squareRoot);
        return Math.toIntExact(Math.round(dist * 1000));
    }

    private double convertDegreesToRadians(double deg) {
        return (deg * Math.PI / 180);
    }

    private double convertRadiansToDegrees(double rad) {
        return (rad * 180 / Math.PI);
    }
}
