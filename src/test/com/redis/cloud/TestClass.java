package com.redis.cloud;

import com.redis.cloud.utils.YHKiss;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RedisApplication.class) // 指定spring-boot的启动类
public class TestClass {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private YHKiss YhKiss;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test1() throws Exception {
        for (int i=0; i<3; i++) {
            async();
        }
    }

    @Test
    public void test2() throws Exception {
        for (int i=0; i<300; i++) {
            boolean rs = YhKiss.checkEnable();
            if (rs) {
                System.out.println("未超");
                stringRedisTemplate.opsForValue().set("test" + i, i + "", 60, TimeUnit.SECONDS);
            } else {
                System.out.println("超了");
            }
        }
    }

    @Async
    public void async() {

    }

    public static void main(String[] args) {

    }

    @Test
    public void runFailure() throws Exception {

    }


    @Test
    public void testRedis() throws Exception {
        for (int i=0; i<1000; i++) {
            this.asyncTest();
        }
//        redisTemplate.execute()
        String key = "uuu";
        String value = "uuu";
        long exptime = 5 * 60;
        Boolean success = (Boolean)redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            return connection.set(keySerializer.serialize(key), valueSerializer.serialize(value), Expiration.from(exptime, TimeUnit.SECONDS), RedisStringCommands.SetOption.ifAbsent());
//            return (Boolean)result;
        });



    }

    @Async
    public void asyncTest() {
        String key = "test";
        String value = "test";
        long exptime = 5 * 60;
        Boolean success = (Boolean)redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            return connection.set(keySerializer.serialize(key), valueSerializer.serialize(value), Expiration.from(exptime, TimeUnit.SECONDS), RedisStringCommands.SetOption.ifAbsent());
        });

        System.out.println(success);
    }

}
