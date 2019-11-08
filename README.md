# redis-demo
redis动态限制

基于第三方接口调用情况，某个第三方接口次数超过限制时，会锁定账号，账号锁定后需要进行一系列麻烦的操作解锁账号，所以会限制在一定时间内第三方接口调用次数。比如，限制1分钟中内调用次数不超过35次，一小时内不超过350次。具体实现思路如下：

 redis提供相关的接口能够统计有效期内key的个数，该方法如下
 	
 	//	 yh1mKey表示前缀，前缀一般固定，比如项目名demo:YH1H，“*”表示后缀任意
    Set<String> keys = stringRedisTemplate.keys(yh1mKey + "*");

只要满足相关的条件，继续调用接口，否则忽略接口调用，不做任何处理，结束调用流程。

1.核心类YHKiss
## java代码:
   	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private int hour = 350;
	private int minute = 35;

    //检查是否超过接口调用次数
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

    //set redis
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

    //统计相应key有效期内在redis的确定个数
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

2.测试类

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


	