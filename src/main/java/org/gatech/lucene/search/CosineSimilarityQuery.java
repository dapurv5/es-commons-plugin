package org.gatech.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;

public class CosineSimilarityQuery extends CustomScoreQuery {

  private Query query;
  private final String field;
  
  public CosineSimilarityQuery(Query subQuery, String field) {
    super(subQuery);
    this.query = subQuery;
    this.field = field;
  }

  public CustomScoreProvider getCustomScoreProvider(final LeafReaderContext context) {
    return new CustomScoreProvider(context) {
      
      public float customScore(int doc, float subQueryScore, float valSrcScore)
          throws IOException {
        Terms terms = context.reader().getTermVector(doc, field);
        TermsEnum te = terms.iterator();
        BytesRef term;
        while((term = te.next()) != null) {
          System.out.println("<"+te.totalTermFreq()+","+term.utf8ToString()+">");
        }
        System.out.println(query.toString());
        System.out.println();
        return valSrcScore;
      }
    };
  }
}
