package com.redis.cloud.utils;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class YHKiss {


	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private int hour = 350;
	private int minute = 35;

	public boolean checkEnable() throws InterruptedException {
		String yh1m = "test:YH1M";
		String yh1h = "test:YH1H";
		KissCount count1m = count(yh1m);
		KissCount count1h = count(yh1h);
		if (count1m.getCount() < minute && count1h.getCount() < hour) {
			this.set(yh1m + "_" + UUID.randomUUID().toString(), "1", 60l);
			this.set(yh1h + "_" + UUID.randomUUID().toString(), "1", 3600l);
			return true;
		} else {
			log.info(
					"YHCX-分钟：" + count1m.getCount() + "，最近等待：" + (count1m.getCount() < minute ? 0 : count1m.getNext()));
			log.info("YHCX-小时：" + count1h.getCount() + "，最近等待：" + (count1h.getCount() < hour ? 0 : count1h.getNext()));
			return false;
		}

	}

	private boolean set(final String key, String value, Long expireTime) {
		boolean result = false;
		try {
			stringRedisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private KissCount count(String yh1mKey) {
		KissCount count = new KissCount();
		Set<String> keys = stringRedisTemplate.keys(yh1mKey + "*");
		count.setCount(keys.size());
		int next = Integer.MAX_VALUE;
		for (String key : keys) {
			if (key != null && get(key) != null) {
				Long e = stringRedisTemplate.getExpire(key);
				next = Math.min(next, e.intValue());
			}
		}
		count.setNext(next);
		return count;
	}

	public Object get(final String key) {
		final String result = stringRedisTemplate.opsForValue().get(key);
		return result;
	}

}
