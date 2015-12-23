package org.gatech.elasticsearch;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
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

    Document doc = new Document();
    TextField textField = new TextField("content", "", Field.Store.YES);

    //You can re-use the same document
    doc.removeField("content");
    textField.setStringValue("humpty dumpty sat on a wall");
    doc.add(textField);

    indexWriter.addDocument(doc);
    indexWriter.commit();

    IndexReader indexReader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    indexSearcher.setSimilarity(customSimilarity);
    QueryParser queryParser = new QueryParser("content", analyzer);
    Query query = queryParser.parse("humpty dumpty");
    TopDocs topDocs = indexSearcher.search(query, 100);
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      doc = indexReader.document(scoreDoc.doc);
      System.out.println(scoreDoc.score + ": " +
          doc.getField("content").stringValue());
    }
  }

}
