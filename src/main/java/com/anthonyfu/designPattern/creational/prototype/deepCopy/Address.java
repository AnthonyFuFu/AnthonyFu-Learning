package com.anthonyfu.designPattern.creational.prototype.deepCopy;

import java.io.Serializable;

//可序列化的引用對象
class Address implements Serializable {
	private static final long serialVersionUID = 2L;
	private String street;
	private String city;

	public Address(String street, String city) {
		this.street = street;
		this.city = city;
	}

	// 標準的 getter/setter
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "Address [street=" + street + ", city=" + city + "]";
	}
}