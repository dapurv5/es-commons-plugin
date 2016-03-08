package org.gatech.lucene.search.util;

public class VectorUtil {
  
  public static float[] zeros(int n) {
    float[] v = new float[n];
    return v;
  }
  
  public static void aggregateSum(float[] agg, float[] elem) {
    for(int i = 0; i < agg.length; i++) {
      agg[i] += elem[i];
    }
  }
  
  public static void divide(float[] vec, int n) {
    for(int i = 0; i < vec.length; i++) {
      vec[i] = vec[i]/n;
    }
  }
  
  public static float magn(float[] vec) {
    float magn = 0;
    for(int i = 0; i < vec.length; i++) {
      magn += vec[i] * vec[i];
    }
    return (float) Math.sqrt(magn);
  }
  
  public static float dot(float[] a, float[] b) {
    //Assert that both have the same length
    float result = 0;
    for(int i = 0; i < a.length; i++) {
      result += a[i] * b[i];
    }
    return result;
  }
}
