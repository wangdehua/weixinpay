package com.wdh.pay.pojo;

import java.math.BigDecimal;

public class RequestPayModel {

	private Integer uid ; //用户编号 , openId 已经放入数据库,需要用uid从数据库取出openid
	
	private Integer goodsId ; //商品编号
	
	private BigDecimal price ; //商品价格
	
	private Integer nums ; //商品数量

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
