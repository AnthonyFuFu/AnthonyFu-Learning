package com.anthonyfu.creationalPatterns.prototype;

//具體原型
class ConcretePrototype implements Prototype {
	private String property;

	public ConcretePrototype(String property) {
		this.property = property;
		System.out.println("ConcretePrototype created with property: " + property);
		// 模擬複雜對象創建過程
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 用於創建克隆對象的私有構造函數
	private ConcretePrototype(ConcretePrototype source) {
		this.property = source.property;
		System.out.println("ConcretePrototype cloned with property: " + property);
	}

	@Override
	public Prototype clone() {
		return new ConcretePrototype(this);
	}

	@Override
	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public String getProperty() {
		return property;
	}
}
