package com.aeon.acss.fdu.controller.impl;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.aeon.acss.fdu.controller.AddressController;
import com.aeon.acss.fdu.request.AddressConvertRequest;
import com.aeon.acss.fdu.request.AddressValidateRequest;
import com.aeon.acss.fdu.response.AddressConvertResponse;
import com.aeon.acss.fdu.response.AddressValidateResponse;
import com.aeon.acss.fdu.service.AddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AddressControllerImpl implements AddressController {

    private final AddressService service;

    public ResponseEntity<AddressValidateResponse> validate(AddressValidateRequest req) {
        return ResponseEntity.ok(service.validate(req));
    }

	public ResponseEntity<Map<String, Object>> reloadProvinceAlias() {
		return ResponseEntity.ok(service.reloadProvinceAliases());
	}

	public ResponseEntity<AddressConvertResponse> inquire(AddressConvertRequest req) {
		return ResponseEntity.ok(service.convert(req));
	}
}
