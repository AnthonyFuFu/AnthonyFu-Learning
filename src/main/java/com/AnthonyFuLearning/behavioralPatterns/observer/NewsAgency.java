package com.AnthonyFuLearning.behavioralPatterns.observer;

import java.util.ArrayList;
import java.util.List;

//具體被觀察者
class NewsAgency implements Subject {
	private List<Observer> observers = new ArrayList<>();
	private String news;

	@Override
	public void registerObserver(Observer observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	@Override
	public void notifyObservers() {
		for (Observer observer : observers) {
			observer.update(news);
		}
	}

	// 設置新聞，並通知所有觀察者
	public void setNews(String news) {
		this.news = news;
		notifyObservers();
	}
}