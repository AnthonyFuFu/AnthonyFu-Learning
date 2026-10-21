package com.anthonyfu;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobRunner implements CommandLineRunner {

	private final JobOperator jobOperator;
	private final Job importMemberJob;
	private final Job importUserJob;
	private final Job importS3FileJob;

	public JobRunner(JobOperator jobOperator,
			@Qualifier("importMemberJob") Job importMemberJob,
			@Qualifier("importUserJob") Job importUserJob,
			@Qualifier("importS3FileJob") Job importS3FileJob) {

		this.jobOperator = jobOperator;
		this.importMemberJob = importMemberJob;
		this.importUserJob = importUserJob;
		this.importS3FileJob = importS3FileJob;
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Starting batch jobs...");
		// 每次啟動都產生新的 JobParameters， 避免相同 JobInstance 已經執行完成而無法再次執行。
//		JobParameters jobParameters = new JobParametersBuilder().addLong("startAt", System.currentTimeMillis()).toJobParameters();

		// 1. 匯入 Member
//		JobExecution memberExecution = jobOperator.start(importMemberJob, jobParameters);
//		log.info("Member batch job launched. Execution ID: " + memberExecution.getId());

		// 2. 匯入 User
//		JobExecution userExecution = jobOperator.start(importUserJob, jobParameters);
//		log.info("User batch job launched. Execution ID: " + userExecution.getId());

		// 3. 匯入 S3 File
//		JobExecution s3FileExecution = jobOperator.start(importS3FileJob, jobParameters);
//		log.info("S3 File batch job launched. Execution ID: " + s3FileExecution.getId());
	}
	
}
