package com.anthonyfu.web.service;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.anthonyfu.web.dto.S3FileDto;
import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;
import com.anthonyfu.web.repository.S3FileRepository;
import com.anthonyfu.web.util.FileUtil;

@Service
public class S3FileService {

	@Autowired
	private S3FileRepository s3FileRepository;

	@Autowired
	private S3StorageService s3StorageService; // 注入 S3 上傳服務

	@Autowired
	private PgS3FileService pgS3FileService; // 注入 PostgreSQL 寫入服務

	@Autowired
	private S3ErrorCsvService s3ErrorCsvService;
	
	@Autowired
	private RestClient restClient;
	
	@Value("${app.base-url}")
	private String baseUrl;
	
	private static final int MAX_FILE_COUNT_PER_CATEGORY = 200;

	private static final List<String> CATEGORY_PREFIXES = List.of("FAB/", "CAD/", "ODB/", "PANEL/", "SMD/", "SPEC/", "GERBER/");
	
	public void addS3File() {

		List<S3FileDto> list = s3FileRepository.findAllProjectedBy();

		if (list == null || list.isEmpty()) {
			System.out.println("查無資料");
			return;
		}
		// =====================================================
		// 測試階段先依年份分組
		// 正式搬資料時，可以再啟用 allList
		// =====================================================

		// List<S3FileDto> allList = new ArrayList<>(list);
		List<S3FileDto> list2015 = list.stream().filter(dto -> "15".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2016 = list.stream().filter(dto -> "16".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2017 = list.stream().filter(dto -> "17".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2018 = list.stream().filter(dto -> "18".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2019 = list.stream().filter(dto -> "19".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();

		List<S3FileDto> list2024 = list.stream().filter(dto -> "24".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2025 = list.stream().filter(dto -> "25".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2026 = list.stream().filter(dto -> "26".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();

		System.out.println("全部資料筆數: " + list.size());
		System.out.println("2015 資料筆數: " + list2015.size());
		System.out.println("2016 資料筆數: " + list2016.size());
		System.out.println("2017 資料筆數: " + list2017.size());
		System.out.println("2018 資料筆數: " + list2018.size());
		System.out.println("2019 資料筆數: " + list2019.size());

		System.out.println("2024 資料筆數: " + list2024.size());
		System.out.println("2025 資料筆數: " + list2025.size());
		System.out.println("2026 資料筆數: " + list2026.size());
		// =====================================================
		// 目前測試階段，可以先各處理第一筆
		// =====================================================
//		processList("2015", list2015);
//		processList("2016", list2016);
//		processList("2017", list2017);
//		processList("2018", list2018);
//		processList("2019", list2019);

//		processList("2024", list2024);
		processList("2025", list2025);
		processList("2026", list2026);
		// 正式搬資料時再使用全部資料
		// processYearList("ALL", allList);
	}
	
//	private void processList(String year, List<S3FileDto> yearList) {
//		if (yearList == null || yearList.isEmpty()) {
//			System.out.println(year + " 查無資料");
//			return;
//		}
//		S3FileDto dto = yearList.get(0);
//
//		System.out.println("========== " + year + " 第一筆資料 ==========");
//		System.out.println("serialID    : " + dto.getSerialID());
//		System.out.println("fPath       : " + dto.getFPath());
//		System.out.println("fSaveName   : " + dto.getFSaveName());
//		System.out.println("fSize       : " + dto.getFSize());
//		System.out.println("accessStart : " + dto.getAccessStart());
//		System.out.println("accessEnd   : " + dto.getAccessEnd());
//		System.out.println("accessUser  : " + dto.getAccessUser());
//		System.out.println("virtualPath : " + dto.getVirtualPath());
//		System.out.println("總筆數      : " + yearList.size());
//		System.out.println("========================================");
//
//		// 測試階段先處理第一筆
//		processFile(dto);
//	}
	private void processList(String year, List<S3FileDto> yearList) {
	    if (yearList == null || yearList.isEmpty()) {
	        System.out.println(year + " 查無資料");
	        return;
	    }
	    System.out.println("========== " + year + " 資料分類開始 ==========");
	    System.out.println(year + " 原始資料筆數: " + yearList.size());

		Map<String, List<S3FileDto>> categoryMap = yearList.stream().collect(Collectors.groupingBy(
				dto -> FileUtil.getFolderPrefix(dto.getFSaveName()), LinkedHashMap::new, Collectors.toList()));
	    /*
	     * 固定依照指定的七個類別處理，
	     * 避免因為資料不存在而漏掉類別。
	     */
	    for (String categoryPrefix : CATEGORY_PREFIXES) {

	        List<S3FileDto> categoryList = categoryMap
	                .getOrDefault(categoryPrefix, List.of())
	                .stream()
	                .limit(MAX_FILE_COUNT_PER_CATEGORY)
	                .toList();

	        processCategoryList(year, categoryPrefix, categoryList);
	    }
	    System.out.println("========== " + year + " 資料分類完成 ==========");
	}
	
	private void processCategoryList(String year, String categoryPrefix, List<S3FileDto> categoryList) {
		String categoryName = categoryPrefix.replace("/", "");
		if (categoryList.isEmpty()) {
			System.out.println(year + " - " + categoryName + " 查無資料");
			return;
		}
		System.out.println(year + " - " + categoryName + " 預計處理筆數: " + categoryList.size());
		for (S3FileDto dto : categoryList) {
			System.out.println("開始處理 [" + year + "][" + categoryName + "] " + dto.getFSaveName());
			processFile(dto);
		}
	}
	
	/**
	 * 處理單一檔案：下載串流 -> S3 上傳 -> PG 寫入
	 */
	public void processFile(S3FileDto dto) {

		String folderPrefix = FileUtil.getFolderPrefix(dto.getFSaveName());
		String s3Key = folderPrefix + dto.getFSaveName();
		long defaultFileSize = dto.getFSize() != null ? dto.getFSize() : 0L; // 先使用 DTO 的檔案大小建立預設 Entity。 如果 HTTP 回應有正確大小，後面會重新建立。
		S3FileEntity defaultFileEntity = buildS3FileEntity(s3Key, dto, defaultFileSize);
		S3FileUploadHistoryEntity defaultHistoryEntity = buildS3FileUploadHistoryEntity(s3Key, dto, defaultFileSize);
		
		try {
			String encodedFileName = URLEncoder.encode(dto.getFPath(), StandardCharsets.UTF_8);
			String downloadUrl = String.format("%s?type=1&serialID=%s&fileName=%s", baseUrl, dto.getSerialID(), encodedFileName);

			restClient.get().uri(URI.create(downloadUrl)).exchange((request, response) -> {

				long responseContentLength = response.getHeaders().getContentLength();
				long contentLength = dto.getFSize() != null ? dto.getFSize() : responseContentLength;
				if (contentLength < 0) {
					contentLength = 0;
				}

				S3FileEntity fileEntity = buildS3FileEntity(s3Key, dto, contentLength);
				S3FileUploadHistoryEntity historyEntity = buildS3FileUploadHistoryEntity(s3Key, dto, contentLength);
				// HTTP 非成功狀態
				if (!response.getStatusCode().is2xxSuccessful()) {
					System.err.println("❌ 下載失敗，HTTP 狀態碼：" + response.getStatusCode().value() + "，檔案：" + dto.getFSaveName());
					s3ErrorCsvService.recordBoth(fileEntity, historyEntity);
					return null;
				}
				// 下載檔案內容
				try (InputStream inputStream = response.getBody()) {
					if (inputStream == null) {
						throw new IllegalStateException("下載回應沒有檔案內容：" + dto.getFSaveName());
					}
					// S3 上傳
					s3StorageService.uploadStream(s3Key, inputStream, contentLength);
				}
				// PostgreSQL 寫入。 此方法內部為 Transaction， 任一張表寫入失敗都會 rollback。
				pgS3FileService.saveFileAndUploadHistory(fileEntity, historyEntity);
				System.out.println("✅ 處理完成：" + dto.getFSaveName());
				return null;
			});

		} catch (Exception e) {
			System.err.println("❌ 檔案處理失敗：" + dto.getFSaveName() + "，原因：" + e.getMessage());
			e.printStackTrace();
			// 可能是： 1. REST 連線失敗 2. S3 上傳失敗 3. PostgreSQL 寫入失敗，因此將 s3_file 與 history 都記錄下來。
			s3ErrorCsvService.recordBoth(defaultFileEntity, defaultHistoryEntity);
		}
	}

	/**
	 * 封裝 Entity 組裝邏輯
	 */
	private S3FileEntity buildS3FileEntity(String s3Key, S3FileDto dto, Long fileSize) {
		S3FileEntity entity = new S3FileEntity();
		entity.setObjectKey(s3Key);
		entity.setBucketName(s3StorageService.getBucketName());
		entity.setFileName(dto.getFSaveName());
		entity.setFileSize(fileSize);
		entity.setPcbNo(FileUtil.parsePcbNo(dto.getFSaveName()));
		entity.setPcbVersion(FileUtil.parsePcbVersion(dto.getFSaveName()));
		entity.setFileVersion("1"); // 舊系統搬移資料，目前指定為第 1 版
		entity.setCreateBy(dto.getAccessUser());
		entity.setUpdateBy(dto.getAccessUser());
		if (dto.getAccessEnd() != null) {
			Timestamp accessEndTimestamp = Timestamp.valueOf(dto.getAccessEnd());
			entity.setCreateDt(accessEndTimestamp);
			entity.setUpdateDt(accessEndTimestamp);
		}
		return entity;
	}

	private S3FileUploadHistoryEntity buildS3FileUploadHistoryEntity(String s3Key, S3FileDto dto, Long fileSize) {

	    S3FileUploadHistoryEntity history = new S3FileUploadHistoryEntity();
	    history.setObjectKey(s3Key);
	    history.setBucketName(s3StorageService.getBucketName());
	    history.setFileName(dto.getFSaveName());
	    history.setFileSize(fileSize);
	    history.setPcbNo(FileUtil.parsePcbNo(dto.getFSaveName()));
	    history.setPcbVersion(FileUtil.parsePcbVersion(dto.getFSaveName()));
	    history.setFileVersion("1"); // 舊系統搬移資料，目前指定為第 1 版
	    history.setUpdateBy(dto.getAccessUser());
	    if (dto.getAccessEnd() != null) {
	        Timestamp accessEndTimestamp = Timestamp.valueOf(dto.getAccessEnd());
	        history.setUpdateDt(accessEndTimestamp);
	    } else {
	        history.setUpdateDt(new Timestamp(System.currentTimeMillis()));
	    }
	    return history;
	}
}
