# es-commons-plugin

Collection of various similarity classes, query types, query parsers on top of
elasticsearch. 

## Starting elasticsearch
export ES_HEAP_SIZE=4g
./bin/elasticsearch -Dsecurity.manager.enabled=false

## Installation
./bin/plugin install file://es-commons-plugin-1.0-plugin.zip

### Scoring Implementations

#### Unit IDF Similarity Model
Add the following line to config/elasticsearch.yml
index.similarity.default.type: unit-idf


#### Simple Cosine Similarity Model
Scores each document by the dot product between the query vector and the document
vector. An example query is given below


```
  {
    "query": {
      "cosine_query": {
        "query": [
          "5770",
          "2724"
        ],
        "field": "codes"
      }
    }
  }
```

#### Stored Vector Dot Product Scoring Model
Retrieves documents by the field_retrieval and scores them by taking the dot product of query vector with the document vector stored in field_scoring.

```
fields": [
    "pid",
    "codes"
  ],
  "query": {
    "stored_vector_product_query": {
      "query": [
        "5770",
        "2724"
      ],
      "field_scoring": "embedding",
      "field_retrieval": "codes",
      "field_magnitude": "magnitude"
    }
  }
 }`

```

#### Precomputed vector dot product query
```
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
      "field_scoring": "embedding",
      "field_retrieval": "codes",
      "field_magnitude": "magnitude"
    }
  }
 }

```
