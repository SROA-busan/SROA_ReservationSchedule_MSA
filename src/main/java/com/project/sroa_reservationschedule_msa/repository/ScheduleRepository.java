package com.project.sroa_reservationschedule_msa.repository;

import com.project.sroa_reservationschedule_msa.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {


    // 엔지니어의 날짜의 일정
    @Query(nativeQuery = true, value = "SELECT s.* FROM schedule s WHERE ((s.start_date like concat('%', ?2, '%') AND s.end_date is null) OR s.end_date like concat('%', ?2, '%'))  AND s.engineer_num= ?1 ORDER BY s.start_date ASC")
    List<Schedule> findAllScheduleTimeByEngineerNumAndDate(Long engineerNum, String date);


    Schedule findByScheduleNum(Long scheduleNum);


    @Transactional
    @Modifying
    @Query("UPDATE Schedule s SET s.endDate=?2, s.status=4  WHERE s.scheduleNum=?1 ")
    void updateForReturn(Long scheduleNum, LocalDateTime dateTime);
}
