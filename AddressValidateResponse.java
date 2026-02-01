package com.aeon.acss.fdu.response;

public class AddressValidateResponse {

    private boolean result;
    private String errorMsg;

    public AddressValidateResponse() {}

    public AddressValidateResponse(boolean result, String errorMsg) {
        this.result = result;
        this.errorMsg = errorMsg;
    }

    public boolean isResult() { return result; }
    public void setResult(boolean result) { this.result = result; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
