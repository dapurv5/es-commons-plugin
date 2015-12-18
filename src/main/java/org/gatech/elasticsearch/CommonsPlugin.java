package org.gatech.elasticsearch;

import org.elasticsearch.index.similarity.CustomSimilarityProvider;
import org.elasticsearch.index.similarity.SimilarityModule;
import org.elasticsearch.plugins.Plugin;

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
    module.addSimilarity(NAME, CustomSimilarityProvider.class);
  }
}
