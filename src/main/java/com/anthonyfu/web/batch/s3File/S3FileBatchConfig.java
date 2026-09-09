package com.anthonyfu.web.batch.s3File;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.anthonyfu.web.model.S3File;
import com.anthonyfu.web.model.S3FileDto;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class S3FileBatchConfig {
    // 1. 定義讀取器 (Reader)
    //    從 user.csv 讀取資料並映射到 User 物件
    @Bean
    ItemReader<S3FileDto> s3FileReader() {
        return new FlatFileItemReaderBuilder<S3FileDto>()
                .name("s3FileReader")
                .resource(new ClassPathResource("s3_file.csv"))
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .quoteCharacter('"')
                .names(
                        "serialID",
                        "nodeType",
                        "fType",
                        "fPath",
                        "fName",
                        "fSaveName",
                        "fSize",
                        "refID",
                        "accessStart",
                        "startMile",
                        "accessEnd",
                        "endMile",
                        "accessType",
                        "accessUser",
                        "overWriteID",
                        "releaseUser",
                        "releaseUserName",
                        "releaseTime",
                        "hisRefID",
                        "virtualPath",
                        "source"
                )
                .targetType(S3FileDto.class)
                .build();
    }

    // 2. 定義處理器 (Processor)
    //    對讀取到的 User 物件進行加工
    @Bean
	ItemProcessor<S3FileDto, S3File> s3FileProcessor() {
		return new S3FileProcessor();
	}

    // 3. 定義寫入器 (Writer)
    //    將處理後的 User 物件寫入資料庫
    @Bean
	ItemWriter<S3File> s3FileWriter(EntityManagerFactory entityManagerFactory) {
		return new JpaItemWriterBuilder<S3File>().entityManagerFactory(entityManagerFactory).build();
	}

    // 4. 定義步驟 (Step)
    //    整合 Reader, Processor, 和 Writer
    @Bean
	Step importS3FileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemReader<S3FileDto> s3FileReader, ItemProcessor<S3FileDto, S3File> s3FileProcessor, ItemWriter<S3File> s3FileWriter) {
        return new StepBuilder("importS3FileStep", jobRepository)
                .<S3FileDto, S3File>chunk(1000)
                .reader(s3FileReader)
                .processor(s3FileProcessor)
                .writer(s3FileWriter)
                .transactionManager(transactionManager)
                .build();
    }

    // 5. 定義作業 (Job)
    //    將 Step 組合起來成為一個完整的 Job
	@Bean
	Job importS3FileJob(JobRepository jobRepository, Step importS3FileStep) {
		return new JobBuilder("importS3FileJob", jobRepository).flow(importS3FileStep).end().build();
	}
}
