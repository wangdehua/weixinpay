package com.wdh.pay.pojo;

import java.math.BigDecimal;

public class RequestPayModel {

	private Integer uid ; //�û���� , openId �Ѿ��������ݿ�,��Ҫ��uid�����ݿ�ȡ��openid
	
	private Integer goodsId ; //��Ʒ���
	
	private BigDecimal price ; //��Ʒ�۸�
	
	private Integer nums ; //��Ʒ����

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Integer getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getNums() {
		return nums;
	}

	public void setNums(Integer nums) {
		this.nums = nums;
	}
	
}
