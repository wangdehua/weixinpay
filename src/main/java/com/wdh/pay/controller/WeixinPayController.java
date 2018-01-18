package com.wdh.pay.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wdh.pay.pojo.RequestPayModel;
import com.wdh.pay.service.WeixinPayService;

@Controller
@RequestMapping(value="/weixin/pay")
public class WeixinPayController {
	
	@Autowired
	private WeixinPayService weixinPayService ;

	/**
	 * index 测试页面
	 * @return
	 */
	@RequestMapping(value="/index")
	@ResponseBody
	public Map<String,Object> index(){
		Map<String,Object> ret = new HashMap<String, Object>() ;
		ret.put("code", "0");
		ret.put("message", "success");
		return ret ;
	}
	
	/**
	 * 统一下单接口  我这里做的是小程序支付
	 * @param model  下单参数
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/prepay")
	@ResponseBody
	public Map<String,String> unifiedOrder(RequestPayModel model, HttpServletRequest request){
		return this.weixinPayService.unifiedOrder(model,request);
	}
	
	/**回调接口必须外网能访问到 ， 否则接不到回调
	 * 
	 * 微信回调接口信息  响应给微信xml格式的数据
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/urlNotify",produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String urlNotify(HttpServletRequest request, HttpServletResponse response) throws Exception{
		return this.weixinPayService.urlNotify(request, response);
	}
}
