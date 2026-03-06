package com.anthonyfu.web.batch.member;

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

import com.anthonyfu.web.model.Member;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class MemberBatchConfig {
    // 1. 定義讀取器 (Reader)
    //    從 members.csv 讀取資料並映射到 Member 物件
    @Bean
    ItemReader<Member> memReader() {
        return new FlatFileItemReaderBuilder<Member>()
                .name("memReader") // 為 Reader 命名
                .resource(new ClassPathResource("members.csv")) // 指定 CSV 檔案位置
                .linesToSkip(1) // 跳過第一行 (標題)
                .delimited() // 使用分隔符解析
                .names("memName", "memAccount", "memPassword") // 定義欄位名稱，對應 Member 的屬性
                .targetType(Member.class) // 設定目標類型
                .build();
    }

    // 2. 定義處理器 (Processor)
    //    對讀取到的 Member 物件進行加工
    @Bean
    ItemProcessor<Member, Member> memProcessor() {
        return new MemberProcessor();
    }

    // 3. 定義寫入器 (Writer)
    //    將處理後的 Member 物件寫入資料庫
    @Bean
    ItemWriter<Member> memWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Member>()
                .entityManagerFactory(entityManagerFactory) // 設定 JPA 的 EntityManagerFactory
                .build();
    }

    // 4. 定義步驟 (Step)
    //    整合 Reader, Processor, 和 Writer
    @Bean
    Step importMemberStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                 ItemReader<Member> reader, ItemProcessor<Member, Member> processor, ItemWriter<Member> writer) {
        return new StepBuilder("importMemberStep", jobRepository)
                .<Member, Member>chunk(10) // 每處理 10 筆資料提交一次事務
                .reader(reader)
                .transactionManager(transactionManager)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // 5. 定義作業 (Job)
    //    將 Step 組合起來成為一個完整的 Job
    @Bean
    Job importMemberJob(JobRepository jobRepository, Step importMemberStep) {
        return new JobBuilder("importMemberJob", jobRepository)
                .flow(importMemberStep) // Job 的執行流程
                .end()
                .build();
    }
}
