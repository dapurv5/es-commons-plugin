package org.gatech.lucene.search.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.openhft.koloboke.collect.map.hash.HashIntFloatMaps;

/**
 * A singleton class which stores the dot products of all code pairs
 * Note: the path to the code file and the products file have been hard-coded.
 */
public class DotProductStore {
  
  private static DotProductStore instance = null;
  private Map<Integer, Float> productMap = HashIntFloatMaps.newMutableMap();
  private CodeIndexStore codeIndexStore = CodeIndexStore.newInstance();
  
  //TODO: Remove hard-coded values
  //private String productsFile = "/nethome/averma80/output/patient_data/gen_code_products/products.txt";
  private String productsFile = "/mnt/production/cdc/gen_code_products/products.txt";
  
  private DotProductStore() throws IOException {
    FileInputStream productsStream = new FileInputStream(new File(productsFile));
    
    BufferedReader productsReader = new BufferedReader(new InputStreamReader(productsStream));
    String line = null;
    while((line = productsReader.readLine()) != null) {
      String[] arr = line.split(",");
      int c1 = Integer.parseInt(arr[0]);
      int c2 = Integer.parseInt(arr[1]);
      int key = cantorPairing(c1, c2);
      if(!productMap.containsKey(key)) {
        productMap.put(key, Float.parseFloat(arr[2]));
      }
    }
  }

  
  /**
   * Maps a pair of integers to a single integer
   * https://en.wikipedia.org/wiki/Pairing_function#Cantor_pairing_function
   */
  private int cantorPairing(int k1, int k2) {
    return (k1+k2)*(k1+k2+1)/2 + k2;
  }
  
  /**
   * Returns the dot product of vector representations of the codes code1 and
   * code2
   */
  public float getProduct(String code1, String code2) {
    int c1 = codeIndexStore.getIndexForCode(code1);
    int c2 = codeIndexStore.getIndexForCode(code2);
    return productMap.get(cantorPairing(c1, c2));
  }
  
  public synchronized static DotProductStore newInstance() throws IOException {
    if(null == instance) {
      instance = new DotProductStore();
    }
    return instance;
  }
}
