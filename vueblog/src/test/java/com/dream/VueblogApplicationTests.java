package com.dream;

import com.dream.pojo.MUser;
import com.dream.service.MUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class VueblogApplicationTests {
    @Autowired
    MUserService userService;
    @Test
    void contextLoads() {
        MUser user = new MUser();
        user.setId(Long.parseLong("1"));
        user.setAvatar("aaaa");
       userService.updateById(user);
        System.out.println(user);
    }

}
