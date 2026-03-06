package com.anthonyfu;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements CommandLineRunner {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job importMemberJob;
    
    @Autowired
    private Job importUserJob;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting the batch job using the confirmed modern method...");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution memberId = jobOperator.start(importMemberJob, jobParameters);

        System.out.println("Batch job has been launched with execution id: " + memberId);

        JobExecution usrId = jobOperator.start(importUserJob, jobParameters);

        System.out.println("Batch job has been launched with execution id: " + usrId);
    }
}