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
	private String mch_id ;		//�̻���
	@Value("${pay.key}")	
	private String key ;		//��Կ
	@Value("${pay.url_notify}")
	private String url_notify;   // ֧���ɹ���Ļص���ַ , ����Ϊ�ܷ��ʵĵ�ַ
	@Value("${pay.url_unified_order}")
	private String url_unified_order; // ͳһ�µ�url
	
	public static final String TIME_FORMAT = "yyyyMMddHHmmss";  //���ڸ�ʽ
	public static final int TIME_EXPIRE = 2; //����ʱ��  ��λ��day
	
	private static Logger log = LoggerFactory.getLogger(WeixinPayServiceImpl.class);
	
	/*
	 * ͳһ�µ�ҵ����
	 * (non-Javadoc)
	 * @see com.wdh.pay.service.WeixinPayService#unifiedOrder(com.wdh.pay.pojo.RequestPayModel, javax.servlet.http.HttpServletRequest)
	 */
	public Map<String, String> unifiedOrder(RequestPayModel model, HttpServletRequest request) {
		Map<String,String> ret = new HashMap<String, String>() ;
		//�����Լ����ݿ�Ķ���
		String orderNumber = OrdersUtils.createOrderNums();
		//��������
		if (!createOrders(orderNumber, model)) {
			// ��������ʧ��!
			ret.put("code", "100");
			ret.put("message", "���ݿⶩ������ʧ��,����ϵ������Ա!");
			return ret ;
		}
		//�µ��߼�
		//��ȡ�����ip
		String clientIp = WeixinPayUtils.getClientIp(request);
		//��ȡ32λ���ַ���
		String randomNonceStr = WeixinPayUtils.generateMixString(32);
		// ����ͳһ�µ��ӿ�
		Map result = unifiedOrder(clientIp , model, randomNonceStr, orderNumber);
		//������Ϣ
		String return_code = (String) result.get("return_code");
		String return_msg = (String) result.get("return_msg");
		if (StringUtils.isNotBlank(return_code) && return_code.equals("SUCCESS")) {
			if (StringUtils.isNotBlank(return_msg) && !return_msg.equals("OK")) {
				// ͳһ�µ�����
				ret.put("code", "101");
				ret.put("message", "ͳһ�µ�ʧ��,����ϵ������Ա!");
				return ret ;
			}
		} else {
			// ͳһ�µ�����
			ret.put("code", "101");
			ret.put("message", "ͳһ�µ�ʧ��,����ϵ������Ա!");
			return ret ;
		}
		//��ȡ��prepayId
		String prepayId = (String) result.get("prepay_id");
		log.info("-------------prepayId----------------"+prepayId);
		if (StringUtils.isBlank(prepayId)) {
			// ͳһ�µ�����
			ret.put("code", "101");
			ret.put("message", "ͳһ�µ���prepayIdΪ��!");
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
				ret.put("message", "���μ���ʧ��....");
				return ret ;
			}
		}
	}
	
	/**
	 * ΢��ͳһ�µ�
	 * @param clientIp
	 * @param model
	 * @param randomNonceStr
	 * @param orderNumber
	 * @return
	 */
	private Map unifiedOrder(String clientIp, RequestPayModel model, String randomNonceStr, String orderNumber) {
		try {
			//��ѯuser��Ϣ   
			//User user = this.userService.selectByPrimaryKey(model.getUid());
			//String openId = user.getOpenId();
			String openid = "o-_gT0afpfnRN0q-t471oQVp6-B4";
			//���� ���������͹���ʱ��
			Date date = new Date();
			String timeStart = WeixinPayUtils.getFormatTime(date, TIME_FORMAT);
			String timeExpire = WeixinPayUtils.getFormatTime(WeixinPayUtils.addDay(date, TIME_EXPIRE), TIME_FORMAT);
			// ��װ�µ����� �ϸ���ѭ  ΢��֧���Ĺ���
			Map<String,String> retData = new HashMap<String, String>() ;
			retData.put("appid", appid);
			retData.put("attach", "΢��֧��-����");
			retData.put("body", "΢��֧��-����");
			retData.put("device_info", "WEB");		
			retData.put("limit_pay", "no_credit");
			retData.put("mch_id", mch_id);
			retData.put("nonce_str", randomNonceStr);
			retData.put("notify_url", url_notify);
			retData.put("openid", openid);
			retData.put("out_trade_no", orderNumber);
			retData.put("sign_type", "MD5");
			retData.put("spbill_create_ip", clientIp);// �����ip��ַ ,������ز���ip��ַ���Ϸ�
			retData.put("spbill_create_ip", "123.123.250.60");
			retData.put("time_expire", timeExpire);
			retData.put("time_start", timeStart);
			retData.put("total_fee", "1");  //��λ��
			retData.put("trade_type", "JSAPI");
			//����ǩ��
			String sign = WeixinPayUtils.generateSignature(retData, key);
			retData.put("sign", sign);
			//����װ�õ�map����  ת��xml��ʽ
			String xml = WeixinPayUtils.mapToXml(retData);
			xml = xml.replace("__", "_").replace("<![CDATA[","").replace("]]>", "");
			log.info(xml);
			// �����µ�����  ���� : ͳһ�µ���url , ����ʽ"POST" , ��װ�õĲ��� xml��ʽ
			StringBuffer buffer = WeixinPayUtils.httpsRequest(url_unified_order, "POST", xml);
			log.info("unifiedOrder request return body: \n" + buffer.toString());
			//���µ����ص�xml��ʽ��Ϣת��map����
			return WeixinPayUtils.xml2Map(buffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/*
	 * �������ݿⶩ����Ϣ
	 */
	private boolean createOrders(String orderNumber, RequestPayModel model) {
		//�������,�ɸ����Լ����󴴽� ....
		log.info("--------------------db orders success-----------------------");
		return true ;
	}

	/**
	 * ΢����Ӧ��֧�����
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
	        log.info("--------------΢����Ӧ������-----------------" + xmlString);
	        Map<String, String> map = new HashMap<String, String>();
	        map = WeixinPayUtils.xml2Map(xmlString) ;
	        String return_code = map.get("return_code");
	    	if (return_code.equals("SUCCESS")) {
				String orderNumber = map.get("out_trade_no");
				log.info("----------------notify ordersNumber-------------------"+orderNumber);
				// ���¶���
				log.info("-------------------db �߼�.....---------------------");
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
