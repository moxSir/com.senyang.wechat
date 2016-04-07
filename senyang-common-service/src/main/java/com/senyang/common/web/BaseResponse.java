package com.senyang.common.web;

public class BaseResponse {
	  public int errcode = 0;
	  public String errmsg;
	    
	  @Override
	  public String toString() {
		  return "BaseResponse{" +
	            "errcode=" + errcode +
	            ", errmsg='" + errmsg + '\'' +
	            '}';
	  }

}
