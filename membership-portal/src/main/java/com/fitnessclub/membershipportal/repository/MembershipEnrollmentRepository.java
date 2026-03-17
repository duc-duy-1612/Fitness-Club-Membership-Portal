package com.fitnessclub.membershipportal.repository;

import com.fitnessclub.membershipportal.entity.MembershipEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipEnrollmentRepository extends JpaRepository<MembershipEnrollment, Integer> {

    List<MembershipEnrollment> findByMember_IdOrderByCreatedAtDesc(Integer memberId);

    List<MembershipEnrollment> findByStatus(String status);

    @Query("SELECT e FROM MembershipEnrollment e LEFT JOIN FETCH e.addOns WHERE e.id = :id")
    MembershipEnrollment findByIdWithAddOns(@Param("id") Integer id);

    @Query(value = "SELECT id FROM membership_enrollments WHERE id = :id", nativeQuery = true)
    Integer findIdNative(@Param("id") Integer id);
}
