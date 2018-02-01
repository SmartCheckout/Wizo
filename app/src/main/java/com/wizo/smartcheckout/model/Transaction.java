package com.wizo.smartcheckout.model;

import java.util.List;

public class Transaction
{
	
	protected String trnsId;
	private Long trnsDate;
	private String status;
	private Long createTS;
	private Long updateTS;
	private Store store;
	private User customer;
	private List<CartItem> cart;
	private Bill bill;


	public Transaction(String trnsId, Long trnsDate, String status, Long createTS, Long updateTS, Store store, List<CartItem> cart, User customer, Bill bill) {
		super();
		this.trnsId = trnsId;
		this.trnsDate = trnsDate;
		this.status = status;
		this.createTS = createTS;
		this.updateTS = updateTS;
		this.store = store;
		this.customer = customer;
		this.cart = cart;
		this.bill = bill;
	}

	public Transaction(){
		super();
	}

	public String getTrnsId() {
		return trnsId;
	}

	public void setTrnsId(String trnsId) {
		this.trnsId = trnsId;
	}

	public Long getTrnsDate() {
		return trnsDate;
	}

	public void setTrnsDate(Long trnsDate) {
		this.trnsDate =  trnsDate;
		;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public User getCustomer() {
		return customer;
	}

	public void setCustomer(User customer) {
		this.customer = customer;
	}

	public List<CartItem> getCart() {
		return cart;
	}

	public void setCart(List<CartItem> cart) {
		this.cart = cart;
	}

	public Bill getBill() {
		return bill;
	}

	public void setBill(Bill bill) {
		this.bill = bill;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCreateTS() {
		return createTS;
	}

	public void setCreateTS(Long createTS) {
		this.createTS = createTS;
	}

	public Long getUpdateTS() {
		return updateTS;
	}

	public void setUpdateTS(Long updateTS) {
		this.updateTS = updateTS;
	}



}
