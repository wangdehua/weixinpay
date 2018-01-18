package com.wdh.pay.service.impl;

import java.io.BufferedReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wdh.pay.pojo.RequestPayModel;
import com.wdh.pay.service.WeixinPayService;
import com.wdh.pay.utils.OrdersUtils;
import com.wdh.pay.utils.WeixinPayUtils;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

	@Value("${pay.appid}")
	private String appid ;		//appid
	@Value("${pay.secret}")
	private String secret ;		//secret
	@Value("${pay.mch_id}")
	private String mch_id ;		//商户号
	@Value("${pay.key}")	
	private String key ;		//密钥
	@Value("${pay.url_notify}")
	private String url_notify;   // 支付成功后的回调地址 , 必须为能访问的地址
	@Value("${pay.url_unified_order}")
	private String url_unified_order; // 统一下单url
	
	public static final String TIME_FORMAT = "yyyyMMddHHmmss";  //日期格式
	public static final int TIME_EXPIRE = 2; //过期时间  单位是day
	
	private static Logger log = LoggerFactory.getLogger(WeixinPayServiceImpl.class);
	
	/*
	 * 统一下单业务处理
	 * (non-Javadoc)
	 * @see com.wdh.pay.service.WeixinPayService#unifiedOrder(com.wdh.pay.pojo.RequestPayModel, javax.servlet.http.HttpServletRequest)
	 */
	public Map<String, String> unifiedOrder(RequestPayModel model, HttpServletRequest request) {
		Map<String,String> ret = new HashMap<String, String>() ;
		//创建自己数据库的订单
		String orderNumber = OrdersUtils.createOrderNums();
		//创建订单
		if (!createOrders(orderNumber, model)) {
			// 订单创建失败!
			ret.put("code", "100");
			ret.put("message", "数据库订单创建失败,请联系开发人员!");
			return ret ;
		}
		//下单逻辑
		//获取请求的ip
		String clientIp = WeixinPayUtils.getClientIp(request);
		//获取32位的字符串
		String randomNonceStr = WeixinPayUtils.generateMixString(32);
		// 调用统一下单接口
		Map result = unifiedOrder(clientIp , model, randomNonceStr, orderNumber);
		//返回信息
		String return_code = (String) result.get("return_code");
		String return_msg = (String) result.get("return_msg");
		if (StringUtils.isNotBlank(return_code) && return_code.equals("SUCCESS")) {
			if (StringUtils.isNotBlank(return_msg) && !return_msg.equals("OK")) {
				// 统一下单错误
				ret.put("code", "101");
				ret.put("message", "统一下单失败,请联系开发人员!");
				return ret ;
			}
		} else {
			// 统一下单错误
			ret.put("code", "101");
			ret.put("message", "统一下单失败,请联系开发人员!");
			return ret ;
		}
		//获取到prepayId
		String prepayId = (String) result.get("prepay_id");
		log.info("-------------prepayId----------------"+prepayId);
		if (StringUtils.isBlank(prepayId)) {
			// 统一下单错误
			ret.put("code", "101");
			ret.put("message", "统一下单的prepayId为空!");
			return ret ;
		} else {
			try {
				String timeStamp = WeixinPayUtils.getFormatTime(new Date(), TIME_FORMAT);
				ret.put("appId", appid);
				ret.put("timeStamp", timeStamp);
				ret.put("nonceStr", randomNonceStr);
				ret.put("package", "prepay_id="+prepayId);
				ret.put("signType", "MD5");
				String sign = WeixinPayUtils.generateSignature(ret, key);
				ret.put("paySign", sign);
				return ret ;
			} catch (Exception e) {
				ret.put("code", "102");
				ret.put("message", "二次加密失败....");
				return ret ;
			}
		}
	}
	
	/**
	 * 微信统一下单
	 * @param clientIp
	 * @param model
	 * @param randomNonceStr
	 * @param orderNumber
	 * @return
	 */
	private Map unifiedOrder(String clientIp, RequestPayModel model, String randomNonceStr, String orderNumber) {
		try {
			//查询user信息   
			//User user = this.userService.selectByPrimaryKey(model.getUid());
			//String openId = user.getOpenId();
			String openid = "o-_gT0afpfnRN0q-t471oQVp6-B4";
			//生成 订单创建和过期时间
			Date date = new Date();
			String timeStart = WeixinPayUtils.getFormatTime(date, TIME_FORMAT);
			String timeExpire = WeixinPayUtils.getFormatTime(WeixinPayUtils.addDay(date, TIME_EXPIRE), TIME_FORMAT);
			// 组装下单数据 严格遵循  微信支付的规则
			Map<String,String> retData = new HashMap<String, String>() ;
			retData.put("appid", appid);
			retData.put("attach", "微信支付-测试");
			retData.put("body", "微信支付-测试");
			retData.put("device_info", "WEB");		
			retData.put("limit_pay", "no_credit");
			retData.put("mch_id", mch_id);
			retData.put("nonce_str", randomNonceStr);
			retData.put("notify_url", url_notify);
			retData.put("openid", openid);
			retData.put("out_trade_no", orderNumber);
			retData.put("sign_type", "MD5");
			retData.put("spbill_create_ip", clientIp);// 请求的ip地址 ,如果本地测试ip地址不合法
			retData.put("spbill_create_ip", "123.123.250.60");
			retData.put("time_expire", timeExpire);
			retData.put("time_start", timeStart);
			retData.put("total_fee", "1");  //金额单位分
			retData.put("trade_type", "JSAPI");
			//生成签名
			String sign = WeixinPayUtils.generateSignature(retData, key);
			retData.put("sign", sign);
			//把组装好的map数据  转成xml格式
			String xml = WeixinPayUtils.mapToXml(retData);
			xml = xml.replace("__", "_").replace("<![CDATA[","").replace("]]>", "");
			log.info(xml);
			// 发送下单请求  参数 : 统一下单的url , 请求方式"POST" , 组装好的参数 xml格式
			StringBuffer buffer = WeixinPayUtils.httpsRequest(url_unified_order, "POST", xml);
			log.info("unifiedOrder request return body: \n" + buffer.toString());
			//把下单返回的xml格式信息转成map类型
			return WeixinPayUtils.xml2Map(buffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/*
	 * 创建数据库订单信息
	 */
	private boolean createOrders(String orderNumber, RequestPayModel model) {
		//这里忽略,可根据自己需求创建 ....
		log.info("--------------------db orders success-----------------------");
		return true ;
	}

	/**
	 * 微信相应的支付结果
	 * @throws Exception 
	 */
	public String urlNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String,String> ret = new HashMap<String, String>() ;
		try {
	        BufferedReader reader = request.getReader();
	        String line = "";
	        String xmlString = null;
	        StringBuffer inputString = new StringBuffer();
	        while ((line = reader.readLine()) != null) {
	            inputString.append(line);
	        }
	        xmlString = inputString.toString();
	        request.getReader().close();
	        log.info("--------------微信响应的数据-----------------" + xmlString);
	        Map<String, String> map = new HashMap<String, String>();
	        map = WeixinPayUtils.xml2Map(xmlString) ;
	        String return_code = map.get("return_code");
	    	if (return_code.equals("SUCCESS")) {
				String orderNumber = map.get("out_trade_no");
				log.info("----------------notify ordersNumber-------------------"+orderNumber);
				// 更新订单
				log.info("-------------------db 逻辑.....---------------------");
			}
			ret.put("return_code", return_code);
			ret.put("return_msg", "OK");
		} catch (Exception e) {
			ret.put("return_code", "SUCCESS");
			ret.put("return_msg", "OK");
		}
		String xml = WeixinPayUtils.mapToXml(ret);
		xml = xml.replace("__", "_");
		log.info("--------------response weixin info--------------------"+xml);
		return xml;
	}
}
