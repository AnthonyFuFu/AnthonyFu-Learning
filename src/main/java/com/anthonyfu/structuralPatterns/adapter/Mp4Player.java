package com.anthonyfu.structuralPatterns.adapter;

class Mp4Player implements AdvancedMediaPlayer {
    @Override
    public void playVlc(String filename) {
        // 不做任何事
    }
    
    @Override
    public void playMp4(String filename) {
        System.out.println("Playing mp4 file: " + filename);
    }
}