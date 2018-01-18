package com.wdh.pay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrdersUtils {

	/** 
     * �����󣬿���Ϊ������� 
     */  
    private static Object lockObj = "lockerOrder";  
    /** 
     * ���������ɼ����� 
     */  
    private static long orderNumCount = 0L;  
    /** 
     * ÿ�������ɶ������������ֵ 
     */  
    private int maxPerMSECSize=1000;  
    /** 
     * ���ɷ��ظ������ţ���������1����1000��������չ 
     * @param tname ������ 
     */  
    public String makeOrderNum() {  
        try {  
            // �������ɵĶ�����  
            String finOrderNum = "";  
            synchronized (lockObj) {  
                // ȡϵͳ��ǰʱ����Ϊ�����ű���ǰ�벿�֣���ȷ������  
                long nowLong = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));  
                // �����������ֵ���㣬����չ����Ŀǰ1���봦���ֵ1000����1��100��  
                if (orderNumCount >= maxPerMSECSize) {  
                    orderNumCount = 0L;  
                }  
                //��װ������  
                String countStr=maxPerMSECSize +orderNumCount+"";  
                finOrderNum=nowLong+countStr.substring(1);  
                orderNumCount++;  
                return finOrderNum;
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
        return  Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()))+"";
    }  
    
    //���ù��������������
    public static String createOrderNums(){
    	return new OrdersUtils().makeOrderNum();
    }
}
