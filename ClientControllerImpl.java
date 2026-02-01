package com.aeon.acss.fdu.controller.impl;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.aeon.acss.fdu.controller.ClientController;
import com.aeon.acss.fdu.model.ClientModel;
import com.aeon.acss.fdu.request.AddressConvertRequest;
import com.aeon.acss.fdu.service.ClientService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ClientControllerImpl implements ClientController {

	private final ClientService clientService;
	@Override
	public String page(Model model) {
		model.addAttribute("pageTitle", "Clients");
		model.addAttribute("content", "client :: content");
		model.addAttribute("activeMenu", "MASTER_CLIENT");
		return "layout/layout";
	}

	@Override
	public ClientModel inquiry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientModel addNew(AddressConvertRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String get() {
		// TODO Auto-generated method stub
		return clientService.get();
	}
}