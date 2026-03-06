package com.anthonyfu.web.batch.user;

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

import com.anthonyfu.web.model.User;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class UserBatchConfig {
    // 1. 定義讀取器 (Reader)
    //    從 user.csv 讀取資料並映射到 User 物件
    @Bean
    ItemReader<User> userReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("userReader") // 為 Reader 命名
                .resource(new ClassPathResource("user.csv")) // 指定 CSV 檔案位置
                .linesToSkip(1) // 跳過第一行 (標題)
                .delimited() // 使用分隔符解析
                .names("usrName", "usrAccount", "usrPassword") // 定義欄位名稱，對應 Member 的屬性
                .targetType(User.class) // 設定目標類型
                .build();
    }

    // 2. 定義處理器 (Processor)
    //    對讀取到的 User 物件進行加工
    @Bean
    ItemProcessor<User, User> userProcessor() {
        return new UserProcessor();
    }

    // 3. 定義寫入器 (Writer)
    //    將處理後的 User 物件寫入資料庫
    @Bean
    ItemWriter<User> userWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory) // 設定 JPA 的 EntityManagerFactory
                .build();
    }

    // 4. 定義步驟 (Step)
    //    整合 Reader, Processor, 和 Writer
    @Bean
    Step importUserStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                 ItemReader<User> reader, ItemProcessor<User, User> processor, ItemWriter<User> writer) {
        return new StepBuilder("importUserStep", jobRepository)
                .<User, User>chunk(10) // 每處理 10 筆資料提交一次事務
                .reader(reader)
                .transactionManager(transactionManager)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // 5. 定義作業 (Job)
    //    將 Step 組合起來成為一個完整的 Job
    @Bean
    Job importUserJob(JobRepository jobRepository, Step importUserStep) {
        return new JobBuilder("importUserJob", jobRepository)
                .flow(importUserStep) // Job 的執行流程
                .end()
                .build();
    }
}
