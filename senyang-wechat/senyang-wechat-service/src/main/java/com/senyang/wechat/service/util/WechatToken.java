package com.senyang.wechat.service.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.peaceful.web.util.GlobalApp;
import com.senyang.common.utils.SenYangContext;

public enum WechatToken {
	INSTANCE,
    TEST {
        @Override
        public String getAccessToken() {
            return "7wr23ZLZz8gF2-e8ZQg2Zek3xlynV9j_GYFaY5M6TAy6Ft_P9CAsS6jWN0jZMsyZQiOb-3EfsdQdTYV1CtyuQKufR007utZ7H3qrgK4fzrw";
        }
    };
    
    private static Logger  logger = LoggerFactory.getLogger(WechatToken.class);
    public static final String APPID = SenYangContext.configContext.getString("wechat-app-id");

    public static final String SECRET = SenYangContext.configContext.getString("wechat-app-secret");

    public static final String WEIXIN_ACCESS_TOKEN_KEY = "WEIXIN_ACCESS_TOKEN";
    public static final String WEIXIN_ACCESS_TOKEN_KEY_TEST ="WEIXIN_ACCESS_TOKEN_TEST";
    public static final String REDISHOST = "haproxy";
    /**
     * 获取token
     */
    public String getAccessToken() {
		  String token = null;
	   /*   if (GlobalApp.isProduct()) {
	          token = Redis.cmd("unknow")
	                  .hget(WEIXIN_ACCESS_TOKEN_KEY, "token");
	      } else {
	          Cache cache = SenYangContext.applicationContext.getBean(Cache.class);
	          token = cache.get(WEIXIN_ACCESS_TOKEN_KEY, String.class);
	          if (token == null) {
	              token = accessToken();
	              JSONObject object = JSONObject.parseObject(token);
	              token = (String) object.get("access_token");
	              cache.put(WEIXIN_ACCESS_TOKEN_KEY, token, 30 * 60 * 2);
	          }
	      }*/
	      
	      return token;
	  
    }

    // 获取微信认证token 一天2w 次
    private String accessToken() {
        String getUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID +
                "&secret=" + SECRET;
        return com.senyang.common.utils.HttpClient.get(getUrl);
    }


    /**
     * 刷新访问token
     */
    public void refreshAccessToken() {
      /*  Cache cache = EdaijiaContext.applicationContext.getBean(Cache.class);
        cache.remove(WEIXIN_ACCESS_TOKEN_KEY);*/
    }
}
