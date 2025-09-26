package org.onebusaway.jmh.util;

public class MemoryPrinter {

  public static void printMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();

    long totalMemory = runtime.totalMemory(); // Total memory allocated to the JVM
    long freeMemory = runtime.freeMemory(); // Free memory within the allocated JVM memory
    long usedMemory = totalMemory - freeMemory; // Used memory within the allocated JVM memory

    System.out.println("  Total Memory: " + toMegabytes(totalMemory) + " MB");
    System.out.println("  Free Memory: " + toMegabytes(freeMemory) + " MB");
    System.out.println("  Used Memory: " + toMegabytes(usedMemory) + " MB");
  }

  private static String toMegabytes(long l) {
    return Long.toString(l / (1024 * 1024));
  }

}
