package com.fitnessclub.membershipportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitnessclub.membershipportal.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    
    // Chỉ cần extends JpaRepository là Spring đã tự động cung cấp sẵn cho bạn
    // các hàm như: findAll() để lấy toàn bộ danh sách, findById() để tìm theo ID,
    // save() để thêm mới/cập nhật, deleteById() để xóa...
    // Bạn chưa cần viết thêm bất kỳ logic nào ở đây lúc này!

}