package org.gatech.lucene.queryparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParsingException;
import org.gatech.lucene.search.PrecomputedVectorDotProductQuery;

/*
 _search POST
 {
  "fields": [
    "pid",
    "codes"
  ],
  "query": {
    "precomputed_vector_product_query": {
      "query": [
        "5770",
        "2724"
      ],
      "field_retrieval": "codes",
    }
  }
 }
 */

public class PrecomputedVectorDotProductQueryParser implements QueryParser {

  @Override
  public String[] names() {
    String name = "precomputed_vector_product_query";
    return new String[] {name, Strings.toCamelCase(name)};
  }

  @Override
  public Query parse(QueryParseContext parseContext) throws IOException,
      QueryParsingException {
    XContentParser parser = parseContext.parser();
    String currentFieldName = null;
    List<String> queryTerms = new ArrayList<>();
    String fieldScoring = null;
    String fieldRetrieval = null;
    
    while(true) {
      XContentParser.Token token;
      if ((token = parser.nextToken()) == XContentParser.Token.END_OBJECT)
        break;
      
      if (token == XContentParser.Token.FIELD_NAME)
        currentFieldName = parser.currentName();
      
      if (token == XContentParser.Token.START_ARRAY) {
        if("query".equals(currentFieldName)) {
          while((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            String term = parser.text();
            
            //TODO: Investigate why this is happening!
            if(term.indexOf("ne_query") > -1) {
              term = term.substring(0, term.indexOf("ne_query"));
            }
            queryTerms.add(term);
          }
        }
      }
      
      if("field_retrieval".equals(currentFieldName)) {
        fieldRetrieval = parser.text();
      }
    }
    
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for(String term : queryTerms) {
      builder.add(new TermQuery(new Term(fieldRetrieval, term)), Occur.SHOULD);
    }
    
    Query query = builder.build();
    PrecomputedVectorDotProductQuery cosQuery = new PrecomputedVectorDotProductQuery(
        query, fieldRetrieval);
    return cosQuery;
  }
}
