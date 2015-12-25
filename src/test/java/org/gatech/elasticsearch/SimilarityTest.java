package org.gatech.elasticsearch;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.elasticsearch.index.similarity.CustomSimilarity;
import org.gatech.lucene.search.CosineSimilarityQuery;
import org.junit.Test;

public class SimilarityTest {

  @Test
  public void test() throws IOException, ParseException {
    Analyzer analyzer = new StandardAnalyzer();
    Directory directory = new RAMDirectory();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    CustomSimilarity customSimilarity = new CustomSimilarity();
    config.setSimilarity(customSimilarity);
    IndexWriter indexWriter = new IndexWriter(directory, config);

    FieldType ftype = new FieldType();
    ftype.setStored(true);
    ftype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ftype.tokenized();
    ftype.setStoreTermVectors(true);
    ftype.setStoreTermVectorPositions(true);
    
    Document doc = new Document();
    Field textField = new Field("content", "", ftype);

    //You can re-use the same document
    doc.removeField("content");
    textField.setStringValue("humpty dumpty sat on a wall, humpty dumpty had a great fall");
    doc.add(textField);

    indexWriter.addDocument(doc);
    indexWriter.commit();

    IndexReader indexReader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    indexSearcher.setSimilarity(customSimilarity);
    QueryParser queryParser = new QueryParser("content", analyzer);
    Query query = queryParser.parse("humpty dumpty");
    CosineSimilarityQuery cosQuery = new CosineSimilarityQuery(query, "content");
    
    TopDocs topDocs = indexSearcher.search(cosQuery, 100);
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      doc = indexReader.document(scoreDoc.doc);
      System.out.println(scoreDoc.score + ": " +
          doc.getField("content").stringValue());
    }
  }

}
