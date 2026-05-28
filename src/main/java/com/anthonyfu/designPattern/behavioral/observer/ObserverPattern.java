package com.anthonyfu.designPattern.behavioral.observer;

public class ObserverPattern {
	public static void main(String[] args) {
		// 創建被觀察者和觀察者
		NewsAgency agency = new NewsAgency();

		NewsChannel channel1 = new NewsChannel("Channel 1");
		NewsChannel channel2 = new NewsChannel("Channel 2");
		NewsChannel channel3 = new NewsChannel("Channel 3");

		// 註冊觀察者
		agency.registerObserver(channel1);
		agency.registerObserver(channel2);
		agency.registerObserver(channel3);

		// 發布新聞，觀察者會收到通知
		agency.setNews("Breaking news: New design pattern article released!");

		// 移除一個觀察者
		agency.removeObserver(channel2);

		// 再次發布新聞
		agency.setNews("Update: The article is trending now!");
	}
}
