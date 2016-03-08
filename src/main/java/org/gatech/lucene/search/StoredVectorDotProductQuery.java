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
import org.gatech.lucene.search.store.CodeEmbeddingStore;
import org.gatech.lucene.search.store.DotProductStore;
import org.gatech.lucene.search.util.VectorUtil;

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
  private final String fieldMagn;
  
  
  private float[] queryEmbedding;
  private float queryEmbeddingMagn = -1;
  
  private final static Logger log = Logger.getLogger(
      StoredVectorDotProductQuery.class);
  
  /**
   * @param subQuery: the query issued
   * @param fieldScoring: the field from which the stored vector has to be read
   */
  public StoredVectorDotProductQuery(Query subQuery, String fieldScoring, String fieldMagn) {
    super(subQuery); //this retrieves documents
    this.query = (BooleanQuery) subQuery;
    this.fieldScoring = fieldScoring;
    this.fieldMagn = fieldMagn;
  }
  
  /**
   * Returns the query embedding as a float vector.
   */
  private float[] getQueryEmbedding() throws IOException {
    if(queryEmbedding != null) {
      return queryEmbedding;
    }
    
    CodeEmbeddingStore embStore = CodeEmbeddingStore.newInstance();
    queryEmbedding = VectorUtil.zeros(embStore.getDimensionality());
    
    int cnt = 0;
    //Collect the terms from the query
    for(BooleanClause clause : query.clauses()) {
      TermQuery termQuery = (TermQuery) clause.getQuery();
      String term = termQuery.getTerm().text();
      float[] emb = embStore.getEmbedding(term);
      VectorUtil.aggregateSum(queryEmbedding, emb);
      cnt++;
    }
    VectorUtil.divide(queryEmbedding, cnt);
    this.queryEmbeddingMagn = VectorUtil.magn(queryEmbedding);
    return queryEmbedding;
  }
  
  
  private float getQueryEmbeddingMagn() {
    return this.queryEmbeddingMagn;
  }

  public CustomScoreProvider getCustomScoreProvider(final LeafReaderContext context) {
    return new CustomScoreProvider(context) {
      
      public float customScore(int doc, float subQueryScore, float valSrcScore)
          throws IOException {
        float[] queryEmbedding = StoredVectorDotProductQuery.this.getQueryEmbedding();
        float queryEmbeddingMagn = StoredVectorDotProductQuery.this.getQueryEmbeddingMagn();
        
        DotProductStore dps = DotProductStore.newInstance();

        Set<String> fieldsToLoad = new HashSet<>();
        fieldsToLoad.add(fieldScoring);
        fieldsToLoad.add(fieldMagn);
        Document document = context.reader().document(doc, fieldsToLoad);

        //fieldScoring scores the document vector
        String[] values = document.getValues(fieldScoring);
        float[] docEmbedding = new float[values.length];
        for(int i = 0; i < values.length; i++) {
          docEmbedding[i] = Float.parseFloat(values[i]);
        }
        float docEmbeddingMagn = Float.parseFloat(document.get(fieldMagn));
        return VectorUtil.dot(queryEmbedding, docEmbedding)/(queryEmbeddingMagn * docEmbeddingMagn);
      }
    };
  }
}
