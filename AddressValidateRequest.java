package com.aeon.acss.fdu.request;

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
public class AddressValidateRequest {
    private String province;
    private String district;
    private String subDistrict;
    private String zipcode;

}
