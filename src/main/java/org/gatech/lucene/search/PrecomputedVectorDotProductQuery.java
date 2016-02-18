package org.gatech.lucene.search;

import java.io.IOException;
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
import org.gatech.lucene.search.store.DotProductStore;

public class PrecomputedVectorDotProductQuery extends CustomScoreQuery {

  private BooleanQuery query;
  private final String fieldRetrieval;
  private final Map<String, Integer> queryVector;
  
  private final static Logger log = Logger.getLogger(
      StoredVectorDotProductQuery.class);
  
  /**
   * @param subQuery: the query issued
   */
  public PrecomputedVectorDotProductQuery(Query subQuery, String fieldRetrieval) {
    super(subQuery); //this retrieves documents
    this.query = (BooleanQuery) subQuery;
    this.fieldRetrieval = fieldRetrieval;
    queryVector = new HashMap<>();
  }
  
  /**
   * Returns the query vector as a map.
   */
  private Map<String, Integer> getQueryVector() throws IOException {
    if(queryVector.size() > 0) {
      return queryVector;
    }    
    
    //Collect the terms from the query
    for(BooleanClause clause : query.clauses()) {
      TermQuery termQuery = (TermQuery) clause.getQuery();
      String term = termQuery.getTerm().text();
      if(!queryVector.containsKey(term)) {
        queryVector.put(term, 0);
      }
      queryVector.put(term, queryVector.get(term) + 1); //increment count by 1
    }
    return queryVector;
  }

  public CustomScoreProvider getCustomScoreProvider(final LeafReaderContext context) {
    return new CustomScoreProvider(context) {
      
      public float customScore(int doc, float subQueryScore, float valSrcScore)
          throws IOException {
        Map<String, Integer> queryTerms = PrecomputedVectorDotProductQuery.this.getQueryVector();
        DotProductStore dps = DotProductStore.newInstance();
        float score = 0;

        Set<String> fieldsToLoad = new HashSet<>();
        fieldsToLoad.add(fieldRetrieval);
        Document document = context.reader().document(doc, fieldsToLoad);
        String[] values = document.get(fieldRetrieval).split(" ");
        
        float Q = 0; //the number of codes in the query
        float D = 0; //the number of codes in the document
        for(String code1 : queryTerms.keySet()) {
          for(String code2 : values) {
            Q += queryTerms.get(code1);
            score += queryTerms.get(code1) * dps.getProduct(code1, code2);
            D += 1;
          }
        }
        score = score/(Q*D);
        return score;
      }
    };
  }
}
