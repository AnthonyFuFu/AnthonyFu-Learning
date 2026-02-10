package com.AnthonyFuLearning.creationalPatterns.prototype;

//測試原型模式
public class PrototypePattern {
	public static void main(String[] args) {
		// 創建原始對象
		Prototype original = new ConcretePrototype("Original");

		// 向管理器註冊原型
		PrototypeManager.addPrototype("original", original);

		// 使用原型創建新對象
		System.out.println("Creating clone...");
		Prototype clone1 = PrototypeManager.getPrototype("original");
		System.out.println("Clone property: " + clone1.getProperty());

		// 修改克隆對象
		clone1.setProperty("Modified Clone");
		System.out.println("Modified clone property: " + clone1.getProperty());
		System.out.println("Original property remains: " + original.getProperty());

		// 再次使用原型創建新對象
		Prototype clone2 = original.clone();
		System.out.println("Clone 2 property: " + clone2.getProperty());
	}
}
