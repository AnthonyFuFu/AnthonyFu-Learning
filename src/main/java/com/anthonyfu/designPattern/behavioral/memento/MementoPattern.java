package com.anthonyfu.designPattern.behavioral.memento;

public class MementoPattern {
	public static void main(String[] args) {
		Originator originator = new Originator();
		CareTaker careTaker = new CareTaker();

		// 設置狀態並保存
		originator.setState("State 1");
		careTaker.addMemento(originator.saveStateToMemento());

		// 變更狀態並再次保存
		originator.setState("State 2");
		careTaker.addMemento(originator.saveStateToMemento());

		// 再次變更狀態
		originator.setState("State 3");

		// 從備忘錄恢復到之前的狀態
		System.out.println("\nCurrent State: " + originator.getState());

		originator.restoreStateFromMemento(careTaker.getMemento(0)); // 恢復到 State 1
		System.out.println("Current State: " + originator.getState());

		originator.restoreStateFromMemento(careTaker.getMemento(1)); // 恢復到 State 2
		System.out.println("Current State: " + originator.getState());
	}
}
