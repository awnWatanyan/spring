package com.aeon.acss.fdu.service.impl;

import org.springframework.stereotype.Service;

import com.aeon.acss.fdu.repository.ClientRepository;
import com.aeon.acss.fdu.service.ClientService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ClientServiceImpl implements ClientService {

	private final ClientRepository clientRepository;
	@Override
	public String get() {
		// TODO Auto-generated method stub
		return clientRepository.findAll().toString();
	}

}
