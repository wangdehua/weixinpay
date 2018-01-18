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
	 * index ����ҳ��
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
	 * ͳһ�µ��ӿ�  ������������С����֧��
	 * @param model  �µ�����
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/prepay")
	@ResponseBody
	public Map<String,String> unifiedOrder(RequestPayModel model, HttpServletRequest request){
		return this.weixinPayService.unifiedOrder(model,request);
	}
	
	/**�ص��ӿڱ��������ܷ��ʵ� �� ����Ӳ����ص�
	 * 
	 * ΢�Żص��ӿ���Ϣ  ��Ӧ��΢��xml��ʽ������
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/urlNotify",produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String urlNotify(HttpServletRequest request, HttpServletResponse response) throws Exception{
		return this.weixinPayService.urlNotify(request, response);
	}
}
