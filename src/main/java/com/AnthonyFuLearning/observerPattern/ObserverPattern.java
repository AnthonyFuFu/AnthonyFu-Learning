package com.AnthonyFuLearning.observerPattern;

import java.util.ArrayList;
import java.util.List;

	interface Observer {
	    void update(String message);
	}

	class Subject {
	    private List<Observer> observers = new ArrayList<>();
	    private String message;

	    public String getMessage() {
	        return message;
	    }

	    public void setMessage(String message) {
	        this.message = message;
	        notifyObservers();
	    }

	    public void attach(Observer observer) {
	        observers.add(observer);
	    }

	    public void detach(Observer observer) {
	        observers.remove(observer);
	    }

	    private void notifyObservers() {
	        for (Observer observer : observers) {
	            observer.update(message);
	        }
	    }
	}

	class Observer1 implements Observer {
	    @Override
	    public void update(String message) {
	        System.out.println("Observer1 received message: " + message);
	    }
	}

	class Observer2 implements Observer {
	    @Override
	    public void update(String message) {
	        System.out.println("Observer2 received message: " + message);
	    }
	}

	public class ObserverPattern {
	    public static void main(String[] args) {
	        Subject subject = new Subject();

	        Observer observer1 = new Observer1();
	        Observer observer2 = new Observer2();

	        subject.attach(observer1);
	        subject.attach(observer2);

	        subject.setMessage("Hello world!");
	        subject.detach(observer2);

	        subject.setMessage("Goodbye world!");
	        
	        subject.attach(observer2);
	        subject.setMessage("Hello world!");
	    }
	}

