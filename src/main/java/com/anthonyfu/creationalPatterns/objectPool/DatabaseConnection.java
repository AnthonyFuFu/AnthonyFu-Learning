package com.anthonyfu.creationalPatterns.objectPool;

//具體可重用對象
class DatabaseConnection implements Reusable {
 private boolean isConnected;
 private String connectionId;
 
 public DatabaseConnection(String connectionId) {
     this.connectionId = connectionId;
     // 模擬耗時的連接創建
     System.out.println("Creating DB connection " + connectionId + "...");
     try {
         Thread.sleep(500);
     } catch (InterruptedException e) {
         e.printStackTrace();
     }
     isConnected = true;
     System.out.println("DB connection " + connectionId + " created");
 }
 
 @Override
 public void reset() {
     System.out.println("Resetting DB connection " + connectionId);
     // 重置對象狀態，但不斷開連接
 }
 
 @Override
 public void operation() {
     if (isConnected) {
         System.out.println("Using DB connection " + connectionId + " to execute query");
     } else {
         System.out.println("Connection " + connectionId + " is not available");
     }
 }
 
 public void close() {
     System.out.println("Closing DB connection " + connectionId);
     isConnected = false;
 }
}
