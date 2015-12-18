package org.elasticsearch.index.similarity;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.index.similarity.AbstractSimilarityProvider;

public class CustomSimilarityProvider extends AbstractSimilarityProvider {

  private final CustomSimilarity similarity = new CustomSimilarity();

  @Inject
  public CustomSimilarityProvider(String name) {
    super(name);
  }

  public CustomSimilarity get() {
    return similarity;
  }
}