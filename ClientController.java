package com.aeon.acss.fdu.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aeon.acss.fdu.model.ClientModel;
import com.aeon.acss.fdu.request.AddressConvertRequest;

@RequestMapping("api/client")
public interface ClientController {

	@GetMapping
	String page(Model model);

	@GetMapping("/inquiry")
	ClientModel inquiry();

	@PostMapping("/addNew")
	ClientModel addNew(@RequestBody AddressConvertRequest request);

	@GetMapping("/get")
	String get();
}
