package com.aeon.acss.fdu.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aeon.acss.fdu.request.AddressConvertRequest;
import com.aeon.acss.fdu.request.AddressValidateRequest;
import com.aeon.acss.fdu.response.AddressConvertResponse;
import com.aeon.acss.fdu.response.AddressValidateResponse;

@RequestMapping("/api/address")
public interface AddressController{

	@PostMapping("/validate")
	ResponseEntity<AddressValidateResponse> validate(@RequestBody AddressValidateRequest req);

	@PostMapping("/reload-province-alias")
	ResponseEntity<Map<String, Object>> reloadProvinceAlias();

	@PostMapping("/inquiry")
	public ResponseEntity<AddressConvertResponse> inquire(@RequestBody AddressConvertRequest req);
}
