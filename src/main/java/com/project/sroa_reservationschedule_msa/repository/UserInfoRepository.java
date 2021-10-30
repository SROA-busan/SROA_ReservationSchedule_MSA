package com.project.sroa_reservationschedule_msa.repository;

import com.project.sroa_reservationschedule_msa.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {


    UserInfo findByuserNum(Long userNum);


}
