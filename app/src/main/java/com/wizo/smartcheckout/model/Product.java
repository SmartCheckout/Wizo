package com.wizo.smartcheckout.model;

public class Product {

	private String uniqueId;
	private String barcode;
	private String title;
	private String description;
	private String category;
	private Double retailPrice;
	private Float discount;
	private Weight weight;

	public Product(String uniqueId, String barcode, String title, String description, String category, Double retailPrice, Float discount,Weight weight) {
		this.uniqueId = uniqueId;
		this.barcode = barcode;
		this.title = title;
		this.description = description;
		this.category = category;
		this.retailPrice = retailPrice;
		this.discount = discount;
		this.weight = weight;

	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Double getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(Double retailPrice) {
		this.retailPrice = retailPrice;
	}

	public Float getDiscount() {
		return discount;
	}

	public void setDiscount(Float discount) {
		this.discount = discount;
	}

	public Double getSellingPrice(){
		return retailPrice - (retailPrice * (discount/100));
	}

	public Double getSavings(){
		return retailPrice - getSellingPrice();
	}

	public Weight getWeight() {
		return weight;
	}

	public void setWeight(Weight weight) {
		this.weight = weight;
	}
}
