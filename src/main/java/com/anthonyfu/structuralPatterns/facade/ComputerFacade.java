package com.anthonyfu.structuralPatterns.facade;

class ComputerFacade {
    private CPU processor;
    private Memory ram;
    private HardDrive hardDrive;
    
    public ComputerFacade() {
        this.processor = new CPU();
        this.ram = new Memory();
        this.hardDrive = new HardDrive();
    }
    
    public void start() {
        processor.freeze();
        ram.load(0, hardDrive.read(0, 1024));
        processor.jump(0);
        processor.execute();
    }
}