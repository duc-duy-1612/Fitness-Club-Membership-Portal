package com.fitnessclub.membershipportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "members") // Tên bảng phải khớp chính xác với database
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tương đương AUTO_INCREMENT trong MySQL
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "health_goals", columnDefinition = "TEXT")
    private String healthGoals;

    // Constructor rỗng (Bắt buộc phải có đối với JPA)
    public Member() {
    }

    // Constructor có tham số (Không chứa id vì id tự tăng)
    public Member(String firstName, String lastName, LocalDate dob, String healthGoals) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.healthGoals = healthGoals;
    }

    // Getters và Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getHealthGoals() {
        return healthGoals;
    }

    public void setHealthGoals(String healthGoals) {
        this.healthGoals = healthGoals;
    }
}