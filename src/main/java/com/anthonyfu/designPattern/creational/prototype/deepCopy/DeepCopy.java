package com.anthonyfu.designPattern.creational.prototype.deepCopy;

public class DeepCopy {
	public static void main(String[] args) {
		// 創建原始對象
		Address address = new Address("123 Main St", "Anytown");
		Person original = new Person("John", address);
		original.addHobby("Reading");
		original.addHobby("Cycling");

		System.out.println("原始對象: " + original);

		// 使用序列化進行深拷貝
		Person deepCopy = original.deepCopy();
		System.out.println("深拷貝對象: " + deepCopy);

		// 修改原始對象的引用字段
		original.getAddress().setStreet("456 New St");
		original.addHobby("Swimming");

		System.out.println("\n修改後的原始對象: " + original);
		System.out.println("深拷貝對象 (應保持不變): " + deepCopy);

		// 手動深拷貝測試
		Person manualCopy = original.manualDeepCopy();
		System.out.println("\n手動深拷貝對象: " + manualCopy);

		// 再次修改原始對象
		original.setName("John Updated");

		System.out.println("\n再次修改後的原始對象: " + original);
		System.out.println("手動深拷貝對象 (應保持不變): " + manualCopy);
	}
}
