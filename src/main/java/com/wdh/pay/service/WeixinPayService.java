package com.wdh.pay.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wdh.pay.pojo.RequestPayModel;

public interface WeixinPayService {

	Map<String, String> unifiedOrder(RequestPayModel model, HttpServletRequest request);

	String urlNotify(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
