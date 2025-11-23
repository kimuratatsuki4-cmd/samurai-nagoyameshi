package com.example.nagoyameshi.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {
    
    // id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    // 予約日時
    @Column(name = "reserved_datetime", nullable = false)
    private LocalDateTime reservedDatetime;
    
    // 予約人数
    @Column(name = "number_of_people", nullable = false)
    private Integer numberOfPeople;
    
    // 予約する店舗（多対一のリレーション）
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    // 予約したユーザー（多対一のリレーション）
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 作成日時
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
    
    // 更新日時
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;
}