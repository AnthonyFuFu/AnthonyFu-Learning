package com.AnthonyFuLearning.creationalPatterns.objectPool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//對象池
class ObjectPool<T extends Reusable> {
	private BlockingQueue<T> pool;
	private int maxSize;
	@SuppressWarnings("unused")
	private Creator<T> creator;

	// 創建對象的函數式接口
	interface Creator<T> {
		T create(int id);
	}

	public ObjectPool(int size, Creator<T> creator) {
		this.maxSize = size;
		this.creator = creator;
		this.pool = new LinkedBlockingQueue<>(maxSize);

		// 預先創建對象填滿池
		for (int i = 0; i < maxSize; i++) {
			pool.add(creator.create(i));
		}
	}

	// 從池中獲取對象
	public T acquire() throws InterruptedException {
		return pool.poll(3, TimeUnit.SECONDS); // 設置超時時間
	}

	// 歸還對象到池
	public void release(T obj) {
		obj.reset(); // 重置對象狀態
		try {
			pool.put(obj); // 放回對象池
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 關閉對象池
	public void shutdown() {
		for (T obj : pool) {
			if (obj instanceof DatabaseConnection) {
				((DatabaseConnection) obj).close();
			}
		}
		pool.clear();
	}
}