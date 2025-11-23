package com.example.nagoyameshi.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Data
@Table(name = "favorites")

public class Favorite {

	// id (主キー)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	// restaurant (外部キー/多対一: 多くのお気に入りが一つの店舗に紐づく)
	@ManyToOne
	@JoinColumn(name = "restaurant_id") // 対応づけるカラム名
	private Restaurant restaurant;

	// user (外部キー/多対一: 多くのお気に入りが一人のユーザーに紐づく)
	@ManyToOne
	@JoinColumn(name = "user_id") // 対応づけるカラム名
	private User user;

	// createdAt (作成日時)
	@Column(name = "created_at", insertable = false, updatable = false) // updatable = false で更新時に対象外とする
	@CreationTimestamp // 作成時に自動でタイムスタンプをセット
	private Timestamp createdAt;

	// updatedAt (更新日時)
	@Column(name = "updated_at", insertable = false, updatable = false)
	@UpdateTimestamp // 更新時に自動でタイムスタンプをセット
	private Timestamp updatedAt;

}
