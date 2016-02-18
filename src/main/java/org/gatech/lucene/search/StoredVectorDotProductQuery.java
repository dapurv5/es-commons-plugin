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

/**
 * Computes the score for a query by taking a dot product of the
 * d-dimensional vector representation of the query and the d-dimensional
 * vector representation for the document.
 *
 * This is basically the same as cosine similarity except that the vector
 * for the document is already stored in the index. The vector for the query
 * is computed at query time.
 * 
 * One important assumption while using this is that the procedure for computing
 * the vector from the query should already be known so the query can be quickly
 * transformed into a d-dimensional query
 */
public class StoredVectorDotProductQuery extends CustomScoreQuery {

  private BooleanQuery query;
  private final String fieldScoring;
  private final Map<String, Integer> queryVector;
  private final int dim;
  
  private final static Logger log = Logger.getLogger(
      StoredVectorDotProductQuery.class);
  
  /**
   * @param subQuery: the query issued
   * @param fieldScoring: the field from which the stored vector has to be read
   * @param dim: the dimensionality of the vector to be constructed
   */
  public StoredVectorDotProductQuery(Query subQuery, String fieldScoring, String dim) {
    super(subQuery); //this retrieves documents
    this.query = (BooleanQuery) subQuery;
    this.fieldScoring = fieldScoring;
    queryVector = new HashMap<>();
    this.dim = Integer.parseInt(dim);
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
      
      private float[] getQueryVector(Map<String, Integer> queryTerms, int dim) {
        float[] queryVector = new float[dim];
        for(int i = 0; i < queryVector.length; i++) {
          queryVector[i] = (float) Math.random();
        }
        return queryVector;
      }
      
      private float dot(float[] a, float[] b) {
        //Assert that both have the same length
        float result = 0;
        for(int i = 0; i < a.length; i++) {
          result += a[i] * b[i];
        }
        return result;
      }
      
      public float customScore(int doc, float subQueryScore, float valSrcScore)
          throws IOException {
        Map<String, Integer> queryTerms = StoredVectorDotProductQuery.this.getQueryVector();
        int dim = StoredVectorDotProductQuery.this.dim;
        
        DotProductStore dps = DotProductStore.newInstance();

        Set<String> fieldsToLoad = new HashSet<>();
        fieldsToLoad.add(fieldScoring);
        Document document = context.reader().document(doc, fieldsToLoad);
        String[] values = document.getValues(fieldScoring);
        float[] docVector = new float[values.length];
        for(int i = 0; i < values.length; i++) {
          docVector[i] = Float.parseFloat(values[i]);
        }
        float[] queryVector = getQueryVector(queryTerms, dim);
        return dot(queryVector, docVector);
      }
    };
  }
}
