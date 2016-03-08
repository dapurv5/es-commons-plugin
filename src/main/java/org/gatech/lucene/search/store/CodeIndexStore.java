package org.gatech.lucene.search.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.openhft.koloboke.collect.map.hash.HashObjIntMap;
import net.openhft.koloboke.collect.map.hash.HashObjIntMaps;

/**
 * A singleton class which stores the index of each diagnostic code
 */
public class CodeIndexStore {
  
  private static CodeIndexStore instance = null;
  HashObjIntMap<String> codeMap = HashObjIntMaps.newMutableMap();
  
  //private String codesFile = "/nethome/averma80/output/patient_data/gen_codes/codes.txt";
  private String codesFile = "/mnt/production/cdc/gen_codes/codes.txt";
  
  private CodeIndexStore() throws IOException {
    FileInputStream codesStream = new FileInputStream(new File(codesFile));
    
    BufferedReader codesReader = new BufferedReader(new InputStreamReader(codesStream));
    String code = null;
    int counter = 0;
    while((code = codesReader.readLine()) != null) {
      if(!codeMap.containsKey(code)) {
        codeMap.put(code, counter);
        counter += 1;
      }
    }
  }
  
  public int getIndexForCode(String code) {
    return codeMap.getInt(code);
  }
  
  public synchronized static CodeIndexStore newInstance() throws IOException {
    if(null == instance) {
      instance = new CodeIndexStore();
    }
    return instance;
  }
}
