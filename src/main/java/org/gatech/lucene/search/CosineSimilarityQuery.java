package org.gatech.lucene.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;


public class CosineSimilarityQuery extends CustomScoreQuery {

  private BooleanQuery query;
  private final String field;
  private final Map<String, Integer> queryVector;
  
  public CosineSimilarityQuery(Query subQuery, String field) {
    super(subQuery);
    this.query = (BooleanQuery) subQuery;
    this.field = field;
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
      
      private float getQueryVectorNorm(Map<String, Integer> queryTerms) {
        float queryVectorNorm = 0;
        for(Integer freq: queryVector.values()) {
          queryVectorNorm = queryVectorNorm + freq*freq;
        }
        return (float) Math.sqrt(queryVectorNorm);
      }
      
      public float customScore(int doc, float subQueryScore, float valSrcScore)
          throws IOException {
        Map<String, Integer> queryTerms = CosineSimilarityQuery.this.getQueryVector();
        
        float qDotD = 0;
        float q = getQueryVectorNorm(queryTerms);
        float d = 0;
                
        Terms terms = context.reader().getTermVector(doc, field);
        TermsEnum te = terms.iterator();
        BytesRef term;
        while((term = te.next()) != null) {
          if(queryTerms.containsKey(term.utf8ToString())) {
            qDotD += te.totalTermFreq() * queryTerms.get(term.utf8ToString());            
          }
          d += te.totalTermFreq() * te.totalTermFreq();
        }
        
        d = (float) Math.sqrt(d);
        return (float) (qDotD/(d*q));
      }
    };
  }
}
