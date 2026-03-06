package com.anthonyfu.web.batch.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.anthonyfu.web.model.Member;

// ItemProcessor<I, O> I: 輸入類型, O: 輸出類型
public class MemberProcessor implements ItemProcessor<Member, Member> {

    private static final Logger log = LoggerFactory.getLogger(MemberProcessor.class);

    @Override
    public Member process(final Member member) throws Exception {
        // 範例處理邏輯：將姓名轉為大寫，並對密碼加上前綴
        final String processedId = member.getMemId();
        final String upperCaseName = member.getMemName().toUpperCase();
        final String processedPassword = "hashed-" + member.getMemPassword();

        final Member transformedMember = new Member(processedId,upperCaseName, member.getMemAccount(), processedPassword);

        log.info("Converting (" + member + ") into (" + transformedMember + ")");

        return transformedMember;
    }
}