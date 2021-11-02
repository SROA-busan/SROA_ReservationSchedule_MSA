package com.project.sroa_reservationschedule_msa.repository;

import com.project.sroa_reservationschedule_msa.model.EngineerInfo;
import com.project.sroa_reservationschedule_msa.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s WHERE s.engineerInfo=?1 AND s.status=?2")
    List<Schedule> findAllByEngineerInfoAndStatus(EngineerInfo engineerInfo, Integer status);

    // 엔지니어의 날짜의 일정
    @Query(nativeQuery = true, value = "SELECT s.* FROM schedule s WHERE ((s.start_date like concat('%', ?2, '%') AND s.end_date is null) OR s.end_date like concat('%', ?2, '%'))  AND s.engineer_num= ?1 ORDER BY s.start_date ASC")
    List<Schedule> findAllScheduleTimeByEngineerNumAndDate(Long engineerNum, String date);

    @Transactional
    @Modifying
    @Query("UPDATE Schedule s SET s.status=?2 WHERE s.scheduleNum=?1")
    void updateStatus(long scheduleNum, Integer status);

    Schedule findByScheduleNum(Long scheduleNum);

    @Transactional
    @Modifying
    @Query("UPDATE Schedule s SET s.endDate=?2 WHERE s.scheduleNum=?1")
    void updateEndDate(Long scheduleNum, Timestamp valueOf);

    @Query(nativeQuery = true, value = "SELECT s.* FROM Schedule s WHERE s.start_date like concat('%', ?2, '%') AND s.engineer_num=?1")
    List<Schedule> findAllByEngineerInfoAndDateTime(Long engineerNum, String dateTime);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM Schedule s WHERE s.engineer_num=?1 AND s.start_date >= ?2")
    Integer findCntByEngineerNumAndDate(Long engineerNum, String beforeDay);

    @Query(nativeQuery = true, value = "SELECT s.* FROM schedule s WHERE s.start_date like concat('%', ?2, '%') AND s.user_num = (\n" +
            "SELECT u.user_num FROM user_info u WHERE u.id=?1 ); ")
    List<Schedule> findAllStartDateByUserIdAndDate(String id, String date);


    @Query(nativeQuery = true, value = "SELECT s.* FROM schedule s WHERE s.end_date like concat('%', ?2, '%') AND s.user_num = (\n" +
            "SELECT u.user_num FROM user_info u WHERE u.id=?1 )")
    List<Schedule> findAllEndDateByUserIdAndDate(String id, String date);

    @Transactional
    @Modifying
    @Query("UPDATE Schedule s SET s.endDate=?2, s.status=4  WHERE s.scheduleNum=?1 ")
    void updateForReturn(Long scheduleNum, LocalDateTime dateTime);
}
