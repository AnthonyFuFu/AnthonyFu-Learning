package com.AnthonyFuLearning.behavioralPatterns.observer;

//被觀察者接口
interface Subject {
	void registerObserver(Observer observer);
	void removeObserver(Observer observer);
	void notifyObservers();
}
