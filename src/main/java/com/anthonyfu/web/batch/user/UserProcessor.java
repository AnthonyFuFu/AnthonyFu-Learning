package com.anthonyfu.web.batch.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.anthonyfu.web.model.User;

// ItemProcessor<I, O> I: 輸入類型, O: 輸出類型
public class UserProcessor implements ItemProcessor<User, User> {

    private static final Logger log = LoggerFactory.getLogger(UserProcessor.class);

    @Override
    public User process(final User user) throws Exception {
        // 範例處理邏輯：將姓名轉為大寫，並對密碼加上前綴
        final String processedId = user.getUsrId();
        final String upperCaseName = user.getUsrName().toUpperCase();
        final String processedPassword = "hashed-" + user.getUsrPassword();

        final User transformedUser = new User(processedId,upperCaseName, user.getUsrAccount(), processedPassword);

        log.info("Converting (" + user + ") into (" + transformedUser + ")");

        return transformedUser;
    }
}