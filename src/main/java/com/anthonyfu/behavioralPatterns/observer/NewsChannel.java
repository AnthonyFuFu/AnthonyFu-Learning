package com.anthonyfu.behavioralPatterns.observer;

//具體觀察者 - 新聞頻道
class NewsChannel implements Observer {
	private String channelName;

	public NewsChannel(String name) {
		this.channelName = name;
	}

	@Override
	public void update(String news) {
		System.out.println(channelName + " received news: " + news);
	}
}
