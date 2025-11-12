package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.RegularHoliday;

@Repository
public interface RegularHolidayRepository extends JpaRepository<RegularHoliday, Integer> {

}
