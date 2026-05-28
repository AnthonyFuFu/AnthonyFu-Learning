package com.anthonyfu.designPattern.structural.adapter;

public class AdapterPattern {
    public static void main(String[] args) {
    	// 適配器模式：用於使不兼容的接口能夠協同工作，如連接舊系統與新系統，或集成第三方庫。
    	// 適配器模式使得原本由於接口不兼容而無法一起工作的類能夠協同工作。
        AudioPlayer audioPlayer = new AudioPlayer();
        
        audioPlayer.play("mp3", "song.mp3");
        audioPlayer.play("mp4", "movie.mp4");
        audioPlayer.play("vlc", "video.vlc");
        audioPlayer.play("avi", "video.avi"); // 不支持的格式
    }
}
