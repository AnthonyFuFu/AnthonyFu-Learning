package com.anthonyfu.web.service;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.anthonyfu.web.config.S3Properties;
import com.anthonyfu.web.dto.S3FileDto;
import com.anthonyfu.web.model.S3FileEntity;
import com.anthonyfu.web.model.S3FileUploadHistoryEntity;
import com.anthonyfu.web.repository.S3FileRepository;
import com.anthonyfu.web.util.FileUtil;
import com.anthonyfu.web.util.S3Util;

@Service
public class S3FileService {

	@Autowired
	private S3FileRepository s3FileRepository;

    @Autowired
    private S3Util s3Util;
    
	@Autowired
	private PgS3FileService pgS3FileService; // 注入 PostgreSQL 寫入服務

	@Autowired
	private S3ErrorCsvService s3ErrorCsvService;

	@Autowired
	private BuildDtoService buildDtoService;
	
	@Autowired
	private RestClient restClient;

    @Autowired
    private S3Properties s3Properties;
    
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
		List<S3FileDto> list2020 = list.stream().filter(dto -> "20".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2021 = list.stream().filter(dto -> "21".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();

		List<S3FileDto> list2022 = list.stream().filter(dto -> "22".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2023 = list.stream().filter(dto -> "23".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2024 = list.stream().filter(dto -> "24".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2025 = list.stream().filter(dto -> "25".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();
		List<S3FileDto> list2026 = list.stream().filter(dto -> "26".equals(FileUtil.getYear(dto.getVirtualPath()))).toList();

		System.out.println("全部資料筆數: " + list.size());
		System.out.println("2015 資料筆數: " + list2015.size());
		System.out.println("2016 資料筆數: " + list2016.size());
		System.out.println("2017 資料筆數: " + list2017.size());
		System.out.println("2018 資料筆數: " + list2018.size());
		System.out.println("2019 資料筆數: " + list2019.size());
		System.out.println("2020 資料筆數: " + list2020.size());
		System.out.println("2021 資料筆數: " + list2021.size());

		System.out.println("2022 資料筆數: " + list2022.size());
		System.out.println("2023 資料筆數: " + list2023.size());
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

		processList("2022", list2022);
		processList("2023", list2023);
		processList("2024", list2024);
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

		Map<String, List<S3FileDto>> categoryMap = yearList.stream().collect(Collectors.groupingBy(dto -> FileUtil.getFolderPrefix(dto.getFSaveName()), LinkedHashMap::new, Collectors.toList()));
		
	    // 固定依照指定的七個類別處理，避免因為資料不存在而漏掉類別。
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
	
	// 處理單一檔案：下載串流 -> S3 上傳 -> PG 寫入
	public void processFile(S3FileDto dto) {

		String folderPrefix = FileUtil.getFolderPrefix(dto.getFSaveName());
		String fSaveName = dto.getFSaveName().toUpperCase(Locale.ROOT);
		String s3Key = folderPrefix + fSaveName;
		long defaultFileSize = dto.getFSize() != null ? dto.getFSize() : 0L; // 先使用 DTO 的檔案大小建立預設 Entity。 如果 HTTP 回應有正確大小，後面會重新建立。
		S3FileEntity defaultFileEntity = buildDtoService.buildS3File(s3Properties.getBucketName(), s3Key, dto, defaultFileSize);
		S3FileUploadHistoryEntity defaultHistoryEntity = buildDtoService.buildS3FileUploadHistory(s3Properties.getBucketName(), s3Key, dto, defaultFileSize);
		
		try {
			boolean dbComplete = pgS3FileService.isFileDataComplete(s3Key);
			boolean s3ObjectExists = s3Util.doesObjectExist(s3Properties.getBucketName(), s3Key);
			System.out.println("檔案：" + fSaveName + "，DB完整：" + dbComplete + "，S3存在：" + s3ObjectExists);
			
			// 狀況一：DB 與 S3 都完整
			if (dbComplete && s3ObjectExists) {
				System.out.println("⏭️ DB 與 S3 都已存在，略過：" + fSaveName);
				return;
			}

			// 狀況二：S3 有，但 DB 沒有或不完整
			if (!dbComplete && s3ObjectExists) {
				System.out.println("📝 S3 已存在，僅補寫 DB：" + fSaveName);
				pgS3FileService.saveFileAndUploadHistory(defaultFileEntity, defaultHistoryEntity);
				return;
			}
		    /*
		     * 走到這裡代表 S3 不存在。兩種情況都需要重新下載並上傳 S3。
		     *
		     * 可能是：
		     * 1. DB 完整、S3 遺失
		     * 2. DB 不完整、S3 也不存在
		     */
			String encodedFileName = URLEncoder.encode(dto.getFPath(), StandardCharsets.UTF_8);
			String downloadUrl = String.format("%s?type=1&serialID=%s&fileName=%s", baseUrl, dto.getSerialID(), encodedFileName);

			restClient.get().uri(URI.create(downloadUrl)).exchange((request, response) -> {

				long responseContentLength = response.getHeaders().getContentLength();
				long contentLength = dto.getFSize() != null ? dto.getFSize() : responseContentLength;
				if (contentLength < 0) {
					contentLength = 0;
				}

				S3FileEntity fileEntity = buildDtoService.buildS3File(s3Properties.getBucketName(), s3Key, dto, contentLength);
				S3FileUploadHistoryEntity historyEntity = buildDtoService.buildS3FileUploadHistory(s3Properties.getBucketName(), s3Key, dto, contentLength);
				// HTTP 非成功狀態
				if (!response.getStatusCode().is2xxSuccessful()) {
					System.err.println("❌ 下載失敗，HTTP 狀態碼：" + response.getStatusCode().value() + "，檔案：" + fSaveName);
					s3ErrorCsvService.recordBoth(fileEntity, historyEntity);
					return null;
				}
				// 下載檔案內容
				try (InputStream inputStream = response.getBody()) {
					if (inputStream == null) {
						throw new IllegalStateException("下載回應沒有檔案內容：" + fSaveName);
					}
					// S3 上傳
					s3Util.putObject(s3Properties.getBucketName(), s3Key, inputStream, contentLength);
				}
				if (!dbComplete) {
					pgS3FileService.saveFileAndUploadHistory(fileEntity, historyEntity);
				}
				System.out.println("✅ 處理完成：" + fSaveName);
				return null;
			});

		} catch (Exception e) {
			System.err.println("❌ 檔案處理失敗：" + fSaveName + "，原因：" + e.getMessage());
			e.printStackTrace();
			// 可能是： 1. REST 連線失敗 2. S3 上傳失敗 3. PostgreSQL 寫入失敗，因此將 s3_file 與 history 都記錄下來。
			s3ErrorCsvService.recordBoth(defaultFileEntity, defaultHistoryEntity);
		}
	}
}
