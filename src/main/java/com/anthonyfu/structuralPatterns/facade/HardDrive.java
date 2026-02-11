package com.anthonyfu.structuralPatterns.facade;

class HardDrive {
    public String read(long lba, int size) {
        System.out.println("HardDrive: Reading data from sector " + lba + " with size " + size);
        return "Data from hard drive";
    }
}
