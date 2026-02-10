package com.AnthonyFuLearning.structuralPatterns.adapter;

class AudioPlayer implements MediaPlayer {
    private MediaAdapter mediaAdapter;
    
    @Override
    public void play(String audioType, String filename) {
        // 原生支持播放 mp3 格式
        if (audioType.equalsIgnoreCase("mp3")) {
            System.out.println("Playing mp3 file: " + filename);
        }
        // 通過適配器支持其他格式
        else if (audioType.equalsIgnoreCase("vlc") || audioType.equalsIgnoreCase("mp4")) {
            mediaAdapter = new MediaAdapter(audioType);
            mediaAdapter.play(audioType, filename);
        } else {
            System.out.println("Invalid media. " + audioType + " format not supported");
        }
    }
}