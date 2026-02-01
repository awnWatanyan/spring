package com.aeon.acss.fdu.service;

import java.util.Map;

import com.aeon.acss.fdu.request.AddressConvertRequest;
import com.aeon.acss.fdu.request.AddressValidateRequest;
import com.aeon.acss.fdu.response.AddressConvertResponse;
import com.aeon.acss.fdu.response.AddressValidateResponse;

public interface AddressService {

	AddressValidateResponse validate(AddressValidateRequest req);

	Map<String, Object> reloadProvinceAliases();

	AddressConvertResponse convert(AddressConvertRequest request);
}
