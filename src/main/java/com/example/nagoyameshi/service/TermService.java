package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.form.TermEditForm;
import com.example.nagoyameshi.repository.TermRepository;

@Service
public class TermService {
	private final TermRepository termRepository;
	
	public TermService(TermRepository termRepository) {
		super();
		this.termRepository = termRepository;
	}

	public Term findFirstTermByOrderByIdDesc() {
		return termRepository.findFirstByOrderByIdDesc();
	}
	
	public void updateTerm(Term term, TermEditForm termEditForm) {
		term.setContent(termEditForm.getContent());
		termRepository.save(term);
	}

}
