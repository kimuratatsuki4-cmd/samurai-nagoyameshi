package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Restaurant;


@Repository
public interface RestaurantRepository  extends JpaRepository<Restaurant, Integer>{
	public Page<Restaurant> findByNameLike(String name, Pageable pageable);
	public Restaurant findFirstByOrderByIdDesc();
	public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable);
}
