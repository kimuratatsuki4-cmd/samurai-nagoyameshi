package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Term;

@Repository
public interface TermRepository extends JpaRepository<Term, Integer> {
	public Term findFirstByOrderByIdDesc();
}
