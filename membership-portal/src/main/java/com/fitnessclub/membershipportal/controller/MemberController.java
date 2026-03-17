package com.fitnessclub.membershipportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitnessclub.membershipportal.entity.Member;
import com.fitnessclub.membershipportal.repository.MemberRepository;

import java.util.List;

@RestController // Đánh dấu đây là Controller trả về dữ liệu dạng JSON
@RequestMapping("/api/members") // Đường dẫn gốc để gọi API này
public class MemberController {

    @Autowired
    private MemberRepository memberRepository; // Nhúng Repository vào để xài

    // API lấy toàn bộ danh sách hội viên
    @GetMapping
    public List<Member> getAllMembers() {
        // Tự động sinh ra câu lệnh SELECT * FROM members và trả về JSON
        return memberRepository.findAll();
    }
}
