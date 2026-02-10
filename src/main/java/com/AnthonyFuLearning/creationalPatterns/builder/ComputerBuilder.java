package com.AnthonyFuLearning.creationalPatterns.builder;

interface ComputerBuilder {
    void buildCPU();
    void buildGPU();
    void buildRAM();
    void buildStorage();
    Computer getComputer();
}