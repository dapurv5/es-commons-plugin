package org.gatech.elasticsearch;

import java.io.IOException;

import org.elasticsearch.index.similarity.CustomSimilarityProvider;
import org.elasticsearch.index.similarity.SimilarityModule;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.plugins.Plugin;
import org.gatech.lucene.queryparser.CosineQueryParser;
import org.gatech.lucene.queryparser.PrecomputedVectorDotProductQueryParser;
import org.gatech.lucene.queryparser.StoredVectorDotProductQueryParser;
import org.gatech.lucene.search.store.DotProductStore;

public class CommonsPlugin extends Plugin {

  public final static String NAME = "es-commons-plugin";
  
  //TODO: Add a check to add the similarity module only if the plugin is enabled
  private final static String ENABLED = "plugins.knapsack.enabled";
  
  @Override
  public String description() {
    return NAME;
  }

  @Override
  public String name() {
    return NAME;
  }

  public void onModule(SimilarityModule module) {
    module.addSimilarity("unit-idf", CustomSimilarityProvider.class);
  }
  
  public void onModule(IndicesModule module) throws IOException {
    module.registerQueryParser(CosineQueryParser.class);
    module.registerQueryParser(StoredVectorDotProductQueryParser.class);
    module.registerQueryParser(PrecomputedVectorDotProductQueryParser.class);
    
    //Load data in memory
    DotProductStore.newInstance();
  }
}
