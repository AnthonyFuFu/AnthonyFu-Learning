package com.AnthonyFuLearning.creationalPatterns.builder;

class GamingComputerBuilder implements ComputerBuilder {
    private Computer computer = new Computer();

    @Override
    public void buildCPU() {
        computer.setCPU("High-end CPU");
    }

    @Override
    public void buildGPU() {
        computer.setGPU("High-end GPU");
    }

    @Override
    public void buildRAM() {
        computer.setRAM(32);
    }

    @Override
    public void buildStorage() {
        computer.setStorage(1024);
    }

    @Override
    public Computer getComputer() {
        return computer;
    }
}