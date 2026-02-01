package com.aeon.acss.fdu.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProvinceAliasRepository {

	private final JdbcTemplate jdbc;

	public ProvinceAliasRepository(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	// DTO แบบเบาๆ (ไม่ใช่ Entity)
	public record ProvinceAliasRow(String provinceTh, String provinceAliases) {
	}

	/**
	 * ตาราง: dbo.province_alias คอลัมน์ที่ใช้: province_th, province_aliases (ไม่มี
	 * is_active)
	 */
	public List<ProvinceAliasRow> findAll() {
		String sql = """
				    SELECT province_th, province_aliases
				    FROM dbo.province_alias
				    ORDER BY province_th
				""";

		return jdbc.query(sql,
				(rs, i) -> new ProvinceAliasRow(rs.getString("province_th"), rs.getString("province_aliases")));
	}
}
