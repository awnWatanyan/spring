package com.aeon.acss.fdu.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AddressRepository {

	private final JdbcTemplate jdbc;

	public AddressRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	// ===== Exists checks =====

	public boolean provinceExists(String provinceTh) {
		Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM dbo.address_thai WHERE province_th = ?", Integer.class,
				provinceTh);
		return cnt != null && cnt > 0;
	}

	public boolean districtExists(String districtTh) {
		Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM dbo.address_thai WHERE district_th = ?", Integer.class,
				districtTh);
		return cnt != null && cnt > 0;
	}

	public boolean subDistrictExists(String subDistrictTh) {
		Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM dbo.address_thai WHERE sub_district_th = ?",
				Integer.class, subDistrictTh);
		return cnt != null && cnt > 0;
	}

	public boolean zipcodeExists(String zipcode) {
		Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM dbo.address_thai WHERE zip_code = ?", Integer.class,
				zipcode);
		return cnt != null && cnt > 0;
	}

	// ===== Match checks =====

	public boolean districtMatchesProvince(String provinceTh, String districtTh) {
		Integer cnt = jdbc.queryForObject(
				"SELECT COUNT(1) FROM dbo.address_thai WHERE province_th = ? AND district_th = ?", Integer.class,
				provinceTh, districtTh);
		return cnt != null && cnt > 0;
	}

	public boolean subDistrictMatchesProvinceDistrict(String provinceTh, String districtTh, String subDistrictTh) {
		Integer cnt = jdbc.queryForObject(
				"SELECT COUNT(1) FROM dbo.address_thai WHERE province_th = ? AND district_th = ? AND sub_district_th = ?",
				Integer.class, provinceTh, districtTh, subDistrictTh);
		return cnt != null && cnt > 0;
	}

	public boolean zipcodeMatchesFull(String provinceTh, String districtTh, String subDistrictTh, String zipcode) {
		Integer cnt = jdbc.queryForObject(
				"SELECT COUNT(1) FROM dbo.address_thai WHERE province_th = ? AND district_th = ? AND sub_district_th = ? AND zip_code = ?",
				Integer.class, provinceTh, districtTh, subDistrictTh, zipcode);
		return cnt != null && cnt > 0;
	}
}
