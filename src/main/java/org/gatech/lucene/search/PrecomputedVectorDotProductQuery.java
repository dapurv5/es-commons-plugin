package org.gatech.lucene.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.gatech.lucene.search.store.CodeEmbeddingStore;
import org.gatech.lucene.search.store.DotProductStore;
import org.gatech.lucene.search.util.VectorUtil;

public class PrecomputedVectorDotProductQuery extends CustomScoreQuery {

  private BooleanQuery query;
  private final String fieldRetrieval;
  private final String fieldScoring;
  private final String fieldMagn;
  
  private final Map<String, Integer> queryVector;
  private float[] queryEmbedding;
  private float queryEmbeddingMagn = -1;
  
  private final static Logger log = Logger.getLogger(
      StoredVectorDotProductQuery.class);
  
  /**
   * @param subQuery: the query issued
   */
  public PrecomputedVectorDotProductQuery(Query subQuery, String fieldRetrieval, String fieldScoring, String fieldMagn) {
    super(subQuery); //this retrieves documents
    this.query = (BooleanQuery) subQuery;
    this.fieldRetrieval = fieldRetrieval;
    this.fieldScoring = fieldScoring;
    this.fieldMagn = fieldMagn;
    queryVector = new HashMap<>();
  }
  
  
  /**
   * Returns the query vector as a map.
   */
  private Map<String, Integer> getQueryVector() throws IOException {
    if(queryVector.size() > 0) {
      return queryVector;
    }
    
    CodeEmbeddingStore embStore = CodeEmbeddingStore.newInstance();
    float[] queryEmb = VectorUtil.zeros(embStore.getDimensionality());
    
    int cnt = 0;
    //Collect the terms from the query
    for(BooleanClause clause : query.clauses()) {
      TermQuery termQuery = (TermQuery) clause.getQuery();
      String term = termQuery.getTerm().text();
      if(!queryVector.containsKey(term)) {
        queryVector.put(term, 0);
      }
      queryVector.put(term, queryVector.get(term) + 1); //increment count by 1
      cnt++;
      
      float[] codeEmb = embStore.getEmbedding(term);
      VectorUtil.aggregateSum(queryEmb, codeEmb);
    }
    
    VectorUtil.divide(queryEmb, cnt);
    this.queryEmbeddingMagn = VectorUtil.magn(queryEmb);
    return queryVector;
  }
  
  
  private float getQueryEmbeddingMagn() {
    return this.queryEmbeddingMagn;
  }
  
  

  public CustomScoreProvider getCustomScoreProvider(final LeafReaderContext context) {
    return new CustomScoreProvider(context) {
      
      public float customScore(int doc, float subQueryScore, float valSrcScore)
          throws IOException {
        Map<String, Integer> queryTerms = PrecomputedVectorDotProductQuery.this.getQueryVector();
        float queryEmbeddingMagn = PrecomputedVectorDotProductQuery.this.getQueryEmbeddingMagn();
        
        DotProductStore dps = DotProductStore.newInstance();
        float score = 0;

        Set<String> fieldsToLoad = new HashSet<>();
        fieldsToLoad.add(fieldRetrieval);
        fieldsToLoad.add(fieldMagn);
        fieldsToLoad.add(fieldScoring);
        Document document = context.reader().document(doc, fieldsToLoad);
        String[] codes = document.get(fieldRetrieval).split(" ");
        
        float Q = 0; //the number of codes in the query
        float D = codes.length; //the number of codes in the document
        for(String code1 : queryTerms.keySet()) {
          Q += queryTerms.get(code1);
          for(String code2 : codes) {            
            score += queryTerms.get(code1) * dps.getProduct(code1, code2);
          }
        }
        
        float docEmbeddingMagn = Float.parseFloat(document.get(fieldMagn));
        score = score/(Q*D);
        
        score = score/(queryEmbeddingMagn * docEmbeddingMagn);
        return score;
      }
    };
  }
}
