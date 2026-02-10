package com.AnthonyFuLearning.creationalPatterns.prototype.deepCopy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//包含引用和集合的主類
class Person implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private Address address; // 引用類型
	private List<String> hobbies; // 集合類型

	public Person(String name, Address address) {
		this.name = name;
		this.address = address;
		this.hobbies = new ArrayList<>();
	}

	// 添加愛好
	public void addHobby(String hobby) {
		hobbies.add(hobby);
	}

	// 標準的 getter/setter
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<String> getHobbies() {
		return hobbies;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", address=" + address + ", hobbies=" + hobbies + "]";
	}

	// 使用序列化實現深拷貝
	public Person deepCopy() {
		try {
			// 將對象寫入字節數組
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();

			// 從字節數組中讀取對象
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (Person) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 手動實現深拷貝 (替代方法)
	public Person manualDeepCopy() {
		// 創建新的地址對象
		Address newAddress = new Address(this.address.getStreet(), this.address.getCity());

		// 創建新的人對象
		Person newPerson = new Person(this.name, newAddress);

		// 複製愛好列表
		for (String hobby : this.hobbies) {
			newPerson.addHobby(hobby);
		}

		return newPerson;
	}
}
