package org.gatech.lucene.search.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import net.openhft.koloboke.collect.map.hash.HashIntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;

public class CodeEmbeddingStore {

  private static CodeEmbeddingStore instance = null;
  
  //map to store the embedding of each code
  private final HashIntObjMap<float[]> embeddingMap = HashIntObjMaps.newMutableMap();
  
  //private String embeddingFile = "/mnt/production/cdc/gen_embeddings_gensim/embeddings.txt";
  private String embeddingFile = "/mnt/production/cdc/gen_embeddings_gensim/embeddings.txt";
  private CodeIndexStore codeIndexStore = CodeIndexStore.newInstance();
  
  private int dim = -1; //the dimensionality of the embedding vector
  
  private CodeEmbeddingStore() throws IOException {
    FileInputStream embStream = new FileInputStream(new File(embeddingFile));
    BufferedReader embReader = new BufferedReader(new InputStreamReader(embStream));
    String line = null;
    while((line = embReader.readLine()) != null) {
      String[] arr = line.split(":");
      String code = arr[0];
      int codeIndex = codeIndexStore.getIndexForCode(code);
      arr = arr[1].split(",");
      float[] vector = new float[arr.length];
      this.dim = arr.length;
      for(int i = 0; i < arr.length; i++) {
        vector[i] = Float.parseFloat(arr[i]);
      }
      embeddingMap.put(codeIndex, vector);
    }
  }
  
  public float[] getEmbedding(String code) {
    return embeddingMap.get(codeIndexStore.getIndexForCode(code));
  }
  
  public int getDimensionality() {
    return this.dim;
  }
  
  public synchronized static CodeEmbeddingStore newInstance() throws IOException{
    if(null == instance) {
      instance = new CodeEmbeddingStore();
    }
    return instance;
  }
}
