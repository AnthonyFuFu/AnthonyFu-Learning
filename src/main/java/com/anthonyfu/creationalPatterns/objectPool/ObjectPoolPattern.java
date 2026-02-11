package com.anthonyfu.creationalPatterns.objectPool;

//測試對象池模式
public class ObjectPoolPattern {
	public static void main(String[] args) throws InterruptedException {
		// 創建一個數據庫連接池
		ObjectPool<DatabaseConnection> connectionPool = new ObjectPool<>(3, id -> new DatabaseConnection("CONN-" + id));

		System.out.println("\n--- Using connections from pool ---");

		// 從池中獲取連接
		DatabaseConnection conn1 = connectionPool.acquire();
		DatabaseConnection conn2 = connectionPool.acquire();

		// 使用連接
		conn1.operation();
		conn2.operation();

		System.out.println("\n--- Releasing connection back to pool ---");

		// 歸還連接
		connectionPool.release(conn1);

		System.out.println("\n--- Acquiring connection again ---");

		// 再次獲取連接（會得到之前歸還的conn1）
		DatabaseConnection conn3 = connectionPool.acquire();
		conn3.operation();

		// 使用完畢後關閉連接池
		connectionPool.release(conn2);
		connectionPool.release(conn3);
		connectionPool.shutdown();
	}
}
