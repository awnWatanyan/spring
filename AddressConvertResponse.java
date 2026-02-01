package com.aeon.acss.fdu.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AddressConvertResponse {

	private String addressNo;
	private String villageBuilding;
	private String room;
	private String floor;
	private String moo;
	private String soi;
	private String road;

	private String province;
	private String district;
	private String subDistrict;
	private String zipcode;

	private String errorMsg;

}
