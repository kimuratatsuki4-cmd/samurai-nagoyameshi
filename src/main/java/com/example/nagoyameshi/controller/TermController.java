package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.service.TermService;

@Controller
public class TermController {
	private final TermService termService;

	public TermController(TermService termService) {
		super();
		this.termService = termService;
	}

	@GetMapping("/terms")
	public String index(Model model) {
		model.addAttribute("term", termService.findFirstTermByOrderByIdDesc());
		return "terms/index";
	}

}
