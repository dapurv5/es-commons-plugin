package org.gatech.tutorial.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

public class LuceneDemo {
  public static void main(String[] args) throws IOException, ParseException {
    RAMDirectory dir = new RAMDirectory();
    
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        new LimitTokenCountAnalyzer(new StandardAnalyzer(), 10000)));
    
    FieldType ftype = new FieldType();
    ftype.setStored(true);
    ftype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ftype.tokenized();
    ftype.setStoreTermVectors(true);
    ftype.setStoreTermVectorPositions(true);
    
    //0th document
    Document doc1 = new Document();
    String content1 = "The quality of mercy is not strained."+
                      "It droppeth as the gentle rain from heaven,"+
                      "Upon the place beneath."+
                      "It is twice blessed."+
                      "It blesseth him that gives and him that takes."+
                      "It is mightiest in the mightiest,"+
                      "It becomes the throned monarch better than his crown."+
                      "His sceptre shows the force of temporal power,"+
                      "An attribute to awe and majesty."+
                      "Wherein doth sit the dread and fear of kings.";
    doc1.add(new Field("isbn", "1", ftype));
    doc1.add(new Field("text", content1, ftype));
    doc1.add(new Field("author", "shakespeare", ftype));
    doc1.add(new Field("play", "the merchant of venice", ftype));
    writer.addDocument(doc1);
    
    //1st document
    Document doc2 = new Document();
    String content2 = "et tu brute";
    doc2.add(new Field("text", content2, ftype));
    doc2.add(new Field("author", "shakespeare", ftype));
    doc2.add(new Field("isbn", "2", ftype));
    doc1.add(new Field("play", "julius caesar", ftype));
    writer.addDocument(doc2);
    
    //2nd document
    Document doc3 = new Document();
    String content3 = "It becomes the throned monarch better than his crown."+
                      "His sceptre shows the force of temporal power,"+
                      "An attribute to awe and majesty."+
                      "Wherein doth sit the dread and fear of kings."+
                      "But mercy is above this sceptred sway,"+
                      "It is enthroned in the hearts of kings,"+
                      "It is an attribute to God himself.";
    doc3.add(new Field("text", content3, ftype));
    doc3.add(new Field("author", "shakespeare", ftype));
    doc3.add(new Field("play", "the merchant of venice 2", ftype));
    doc3.add(new Field("isbn", "3", ftype));
    writer.addDocument(doc3);
    writer.close();
    
    DirectoryReader reader = DirectoryReader.open(dir);
    QueryParser parser = new QueryParser("text", 
        new StandardAnalyzer());
    Query query = parser.parse("mercy");
    
    IndexSearcher is = new IndexSearcher(reader);
    TopDocs hits = is.search(query, 10);
    for(ScoreDoc scoreDoc : hits.scoreDocs){
      Document doc = is.doc(scoreDoc.doc);
      Explanation explanation = is.explain(query, scoreDoc.doc);
      System.out.println("-------------");
      System.out.println("match in " + doc.get("play"));
      System.out.println(explanation.toString());
      System.out.println(scoreDoc.score);
    }
    reader.close();
  }
}
