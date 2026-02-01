package com.aeon.acss.fdu.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.aeon.acss.fdu.repository.AddressRepository;
import com.aeon.acss.fdu.repository.ProvinceAliasRepository;
import com.aeon.acss.fdu.request.AddressConvertRequest;
import com.aeon.acss.fdu.request.AddressValidateRequest;
import com.aeon.acss.fdu.response.AddressConvertResponse;
import com.aeon.acss.fdu.response.AddressValidateResponse;
import com.aeon.acss.fdu.service.AddressService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

	private static final Logger log = LoggerFactory.getLogger(AddressService.class);

	private final AddressRepository addressRepo;
	private final ProvinceAliasRepository provinceAliasRepo;

	// key = alias/ชื่อจังหวัดที่ user ส่งมา, value = ชื่อจังหวัดมาตรฐาน
	// (province_th)
	private final Map<String, String> provinceAliasToCanonical = new ConcurrentHashMap<>();


	@PostConstruct
	public void init() {
		// โหลด alias ตอน start (กันแอปล้มถ้า DB/ตารางยังไม่พร้อม)
		try {
			reloadProvinceAliases();
		} catch (Exception ex) {
			log.warn("Province alias preload failed. App will continue with empty alias cache. Cause: {}",
					ex.getMessage());
		}
	}

	/**
	 * Manual reload endpoint จะเรียกอันนี้ได้
	 */
	public Map<String, Object> reloadProvinceAliases() {
		provinceAliasToCanonical.clear();

		int provinceCount = 0;
		int aliasCount = 0;

		List<ProvinceAliasRepository.ProvinceAliasRow> rows = provinceAliasRepo.findAll();

		for (ProvinceAliasRepository.ProvinceAliasRow r : rows) {
			String canonical = safeTrim(r.provinceTh());
			if (canonical.isEmpty())
				continue;

			provinceCount++;

			Set<String> aliases = new LinkedHashSet<>();
			aliases.add(canonical);

			String csv = safeTrim(r.provinceAliases());
			if (!csv.isEmpty()) {
				String[] parts = csv.split("[,;|\\n\\r]+");
				for (String p : parts) {
					String a = safeTrim(p);
					if (!a.isEmpty())
						aliases.add(a);
				}
			}

			for (String a : aliases) {
				provinceAliasToCanonical.put(a, canonical);
			}
			aliasCount += Math.max(0, aliases.size() - 1);
		}

		Map<String, Object> info = new LinkedHashMap<>();
		info.put("provinceCount", provinceCount);
		info.put("aliasCount", aliasCount);
		return info;
	}

	// ====== Controller เรียกอันนี้ ======
	public AddressValidateResponse validate(AddressValidateRequest req) {
		String provinceIn = safeTrim(req.getProvince());
		String district = safeTrim(req.getDistrict());
		String subDistrict = safeTrim(req.getSubDistrict());
		String zipcode = safeTrim(req.getZipcode());

		// แปลงจังหวัดจาก alias -> canonical
		String province = resolveProvince(provinceIn);

		List<String> errors = new ArrayList<>();

		// ---- Exists checks ----
		boolean provinceOk = true, districtOk = true, subOk = true, zipOk = true;

		if (!provinceIn.isEmpty()) {
			provinceOk = addressRepo.provinceExists(province);
			if (!provinceOk)
				errors.add("province:" + provinceIn + " not found");
		}

		if (!district.isEmpty()) {
			districtOk = addressRepo.districtExists(district);
			if (!districtOk)
				errors.add("district:" + district + " not found");
		}

		if (!subDistrict.isEmpty()) {
			subOk = addressRepo.subDistrictExists(subDistrict);
			if (!subOk)
				errors.add("sub_district:" + subDistrict + " not found");
		}

		if (!zipcode.isEmpty()) {
			zipOk = addressRepo.zipcodeExists(zipcode);
			if (!zipOk)
				errors.add("zipcode:" + zipcode + " not found");
		}

		// ---- Match checks ----

		// district ต้องอยู่ใน province
		if (!provinceIn.isEmpty() && !district.isEmpty() && provinceOk && districtOk) {
			if (!addressRepo.districtMatchesProvince(province, district)) {
				errors.add("district:" + district + " province:" + provinceIn + " not match");
			}
		}

		// subDistrict ต้องอยู่ใน province + district
		if (!provinceIn.isEmpty() && !district.isEmpty() && !subDistrict.isEmpty() && provinceOk && districtOk
				&& subOk) {
			if (!addressRepo.subDistrictMatchesProvinceDistrict(province, district, subDistrict)) {
				errors.add("sub_district:" + subDistrict + " district:" + district + " not match");
			}
		}

		// zipcode ต้อง match full
		if (!provinceIn.isEmpty() && !district.isEmpty() && !subDistrict.isEmpty() && !zipcode.isEmpty() && provinceOk
				&& districtOk && subOk && zipOk) {
			if (!addressRepo.zipcodeMatchesFull(province, district, subDistrict, zipcode)) {
				errors.add("zipcode:" + zipcode + " not match");
			}
		}

		if (errors.isEmpty())
			return new AddressValidateResponse(true, "");
		return new AddressValidateResponse(false, String.join(",", errors));
    }
	public AddressConvertResponse convert(AddressConvertRequest request) {
		// 1. รวมทุก Field เป็น String เดียว
		String fullAddress = Stream
				.of(request.getAddress(), request.getSubDistrict(), request.getDistrict(), request.getProvince(),
						request.getZipcode())
				.filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining(" ")).replaceAll("\\s+", " ").trim();

		AddressConvertResponse.AddressConvertResponseBuilder builder = AddressConvertResponse.builder();
		String work = fullAddress;

		// 2. สกัดข้อมูลเฉพาะ (Address Details) ออกก่อน เพื่อไม่ให้รบกวนการ Pop จากท้าย
		// เลขที่
		work = extractAndSet(work, "เลขที่\\s*([\\d/\\-]+)", builder::addressNo);
		// หมู่ (ดักจับทั้ง "หมู่ 5" และ "หมู่5")
		work = extractAndSet(work, "หมู่(?:ที่)?\\s*(\\d+)", builder::moo);
		// ชั้น / ห้อง
		work = extractAndSet(work, "ชั้น\\s*(\\d+)", builder::floor);
		work = extractAndSet(work, "ห้อง\\s*(\\d+)", builder::room);
		// ซอย
		work = extractAndSet(work, "(?:ซอย|ซ\\.)\\s*([^\\s]+)", builder::soi);
		// ถนน
		work = extractAndSet(work, "(?:ถนน|ถ\\.)\\s*([^\\s]+)", builder::road);

		// 3. สกัด Zipcode
		Pattern zipPattern = Pattern.compile("(\\d{5})");
		Matcher zipMatcher = zipPattern.matcher(work);
		if (zipMatcher.find()) {
			builder.zipcode(zipMatcher.group(1));
			work = work.replace(zipMatcher.group(0), "");
		}

		// 4. ล้างคำนำหน้า (จ./ต./อ./เขต/แขวง) ออกให้เหลือแต่ชื่อเพียวๆ เพื่อเตรียม Pop
		work = work.replaceAll("(จังหวัด|จ\\.|อำเภอ|อ\\.|ตำบล|ต\\.|เขต|แขวง)", " ");

		// 5. Pop จากหลังไปหน้า (ใช้ Tokenize)
		List<String> tokens = new ArrayList<>(Arrays.asList(work.trim().split("\\s+")));

		// จังหวัด (ท้ายสุด)
		if (!tokens.isEmpty()) {
			builder.province(tokens.remove(tokens.size() - 1));
		}

		// subDistrict (อำเภอ/แขวง - รองท้าย)
		if (!tokens.isEmpty()) {
			builder.subDistrict(tokens.remove(tokens.size() - 1));
		}

		// district (ตำบล/เขต - ถัดขึ้นมา)
		if (!tokens.isEmpty()) {
			builder.district(tokens.remove(tokens.size() - 1));
		}

		// 6. ส่วนที่เหลือหน้าสุดคือ villageBuilding
		if (!tokens.isEmpty()) {
			builder.villageBuilding(String.join(" ", tokens));
		}

		AddressConvertResponse response = builder.build();
		// Validation ตามเงื่อนไข
		if (response.getProvince() == null)
			response.setProvince("-");
		if (response.getSubDistrict() == null)
			response.setSubDistrict("-");

		return response;
	}

	private String extractAndSet(String text, String regex, java.util.function.Consumer<String> setter) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		if (m.find()) {
			setter.accept(m.group(1).trim());
			// ตัดส่วนที่เจอทิ้งไป
			return text.replace(m.group(0), " ").replaceAll("\\s+", " ").trim();
		}
		return text;
	}

	private void parseDetailSection(String text, AddressConvertResponse.AddressConvertResponseBuilder builder) {
		// ใช้ Regex แกะส่วนปลีกย่อยจากส่วนที่เหลือด้านหน้า
		String work = text;
		work = findAndSet(work, "เลขที่\\s*([\\d/\\-]+)", builder::addressNo);
		work = findAndSet(work, "หมู่(?:ที่)?\\s*(\\d+)", builder::moo);
		work = findAndSet(work, "ชั้น\\s*(\\d+)", builder::floor);
		work = findAndSet(work, "ห้อง\\s*(\\d+)", builder::room);
		work = findAndSet(work, "(?:ซอย|ซ\\.)\\s*([^\\s]+)", builder::soi);
		work = findAndSet(work, "(?:ถนน|ถ\\.)\\s*([^\\s]+)", builder::road);

		// ถ้าเหลืออะไรอีก ให้เป็นชื่อหมู่บ้านหรืออาคาร
		if (!work.trim().isEmpty()) {
			builder.villageBuilding(work.trim());
		}
	}

	private String findAndSet(String text, String regex, java.util.function.Consumer<String> setter) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		if (m.find()) {
			setter.accept(m.group(1).trim());
			return text.replace(m.group(0), "").trim();
		}
		return text;
	}

	private String extractDistrictInfo(String text, AddressConvertResponse.AddressConvertResponseBuilder builder) {
		// หา District (ในที่นี้คือ ตำบล/ต./เขต ตามโจทย์)
		String distRegex = "(?:ตำบล|ต\\.|เขต)\\s*([^\\s]+)";
		Pattern distP = Pattern.compile(distRegex);
		Matcher distM = distP.matcher(text);
		if (distM.find()) {
			builder.district(distM.group(1).trim());
			text = text.replace(distM.group(0), "");
		}

		// หา SubDistrict (ในที่นี้คือ อำเภอ/อ./แขวง ตามโจทย์)
		String subDistRegex = "(?:อำเภอ|อ\\.|แขวง)\\s*([^\\s]+)";
		Pattern subDistP = Pattern.compile(subDistRegex);
		Matcher subDistM = subDistP.matcher(text);
		if (subDistM.find()) {
			builder.subDistrict(subDistM.group(1).trim());
			text = text.replace(subDistM.group(0), "");
		}
		return text.trim();
	}

	private String handleNoPrefixCase(String text, AddressConvertResponse.AddressConvertResponseBuilder builder) {
		// ถ้าข้อมูล District หรือ SubDistrict ยังว่างอยู่ ให้ดึงจากคำท้ายประโยค
		String[] words = text.split("\\s+");
		if (words.length >= 2) {
			if (builder.build().getSubDistrict() == null && builder.build().getDistrict() == null) {
				// คำสุดท้ายเป็น SubDistrict (อำเภอ), ก่อนสุดท้ายเป็น District (ตำบล)
				// ตามโจทย์ข้อ 6
				builder.subDistrict(words[words.length - 1]);
				builder.district(words[words.length - 2]);

				// ลบคำที่ใช้ออกไป
				text = text.replace(words[words.length - 1], "").replace(words[words.length - 2], "");
			}
		}
		return text.trim();
	}

	private void parseBasicInfo(String text, AddressConvertResponse.AddressConvertResponseBuilder builder) {
		String working = text;

		// ใช้ Regex จากที่เคยเขียนไว้ข้างต้น
		working = findAndSet(working, "เลขที่\\s*([\\d/\\-]+)", builder::addressNo);
		working = findAndSet(working, "หมู่(?:ที่)?\\s*(\\d+)", builder::moo);
		working = findAndSet(working, "ชั้น\\s*(\\d+)", builder::floor);
		working = findAndSet(working, "ห้อง\\s*(\\d+)", builder::room);
		working = findAndSet(working, "(?:ซอย|ซ\\.)\\s*([^ถ\\s]+)", builder::soi);
		working = findAndSet(working, "(?:ถนน|ถ\\.)\\s*(\\S+)", builder::road);

		// ส่วนที่เหลือจริงๆ (อาคาร/บริษัท)
		String remains = working.replaceAll("\\s+", " ").trim();
		if (!remains.isEmpty()) {
			builder.villageBuilding(remains);
		}
	}

	// ===== helpers =====

	private String resolveProvince(String provinceIn) {
		String p = safeTrim(provinceIn);
		if (p.isEmpty())
			return "";
		String canonical = provinceAliasToCanonical.get(p);
		return (canonical != null) ? canonical : p;
    }

	private String safeTrim(String s) {
		return (s == null) ? "" : s.trim();
    }
}
