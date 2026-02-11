package com.anthonyfu.structuralPatterns.adapter;

class VlcPlayer implements AdvancedMediaPlayer {
    @Override
    public void playVlc(String filename) {
        System.out.println("Playing vlc file: " + filename);
    }
    
    @Override
    public void playMp4(String filename) {
        // 不做任何事
    }
}
