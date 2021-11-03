package com.project.sroa_reservationschedule_msa.repository;

import com.project.sroa_reservationschedule_msa.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {




    @Query("SELECT u FROM UserInfo  u WHERE u.id=?1")
    UserInfo findByuserId(String userId);
}
