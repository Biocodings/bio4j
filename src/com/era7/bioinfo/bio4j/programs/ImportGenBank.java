/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.bio4j.programs;

import com.era7.bioinfo.bio4j.CommonData;
import com.era7.bioinfo.bio4jmodel.nodes.refseq.CDSNode;
import com.era7.bioinfo.bio4jmodel.nodes.refseq.GeneNode;
import com.era7.bioinfo.bio4jmodel.nodes.refseq.GenomeElementNode;
import com.era7.bioinfo.bio4jmodel.nodes.refseq.rna.*;
import com.era7.bioinfo.bio4jmodel.relationships.refseq.*;
import com.era7.lib.bioinfo.bioinfoutil.Executable;
import com.era7.lib.bioinfo.bioinfoutil.genbank.GBCommon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPInputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.neo4j.graphdb.index.BatchInserterIndex;
import org.neo4j.graphdb.index.BatchInserterIndexProvider;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.impl.lucene.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.impl.batchinsert.BatchInserter;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class ImportGenBank implements Executable {

    //--------indexing API constans-----
    private static String PROVIDER_ST = "provider";
    private static String EXACT_ST = "exact";
    private static String FULL_TEXT_ST = "fulltext";
    private static String LUCENE_ST = "lucene";
    private static String TYPE_ST = "type";
    //-----------------------------------
    public static final String BASE_FOLDER = "refseq/release/complete/";
    private static final Logger logger = Logger.getLogger("ImportGenBank");
    private static FileHandler fh;
    
    //------------------nodes properties maps-----------------------------------
    public static Map<String, Object> genomeElementProperties = new HashMap<String, Object>();
    public static Map<String, Object> geneProperties = new HashMap<String, Object>();
    public static Map<String, Object> cdsProperties = new HashMap<String, Object>();
    public static Map<String, Object> miscRnaProperties = new HashMap<String, Object>();
    public static Map<String, Object> mRnaProperties = new HashMap<String, Object>();
    public static Map<String, Object> ncRnaProperties = new HashMap<String, Object>();
    public static Map<String, Object> rRnaProperties = new HashMap<String, Object>();
    public static Map<String, Object> tmRnaProperties = new HashMap<String, Object>(); 
    public static Map<String, Object> tRnaProperties = new HashMap<String, Object>();  
    //----------------------------------------------------------------------------------
    
    //--------------------------------relationships------------------------------------------
    public static GenomeElementGeneRel genomeElementGeneRel = new GenomeElementGeneRel(null);
    public static GenomeElementCDSRel genomeElementCDSRel = new GenomeElementCDSRel(null);
    public static GenomeElementMiscRnaRel genomeElementMiscRnaRel = new GenomeElementMiscRnaRel(null);
    public static GenomeElementMRnaRel genomeElementMRnaRel = new GenomeElementMRnaRel(null);
    public static GenomeElementNcRnaRel genomeElementNcRnaRel = new GenomeElementNcRnaRel(null);
    public static GenomeElementRRnaRel genomeElementRRnaRel = new GenomeElementRRnaRel(null);
    public static GenomeElementTmRnaRel genomeElementTmRnaRel = new GenomeElementTmRnaRel(null);
    public static GenomeElementTRnaRel genomeElementTRnaRel = new GenomeElementTRnaRel(null);
    //----------------------------------------------------------------------------------

    @Override
    public void execute(ArrayList<String> array) {
        String[] args = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            args[i] = array.get(i);
        }
        main(args);
    }

    public static void main(String[] args) {


        File currentFolder = new File(".");

        File[] files = currentFolder.listFiles();

        BatchInserter inserter = null;
        BatchInserterIndexProvider indexProvider = null;


        //----------------------------------------------------------------------------------
        //---------------------initializing node type properties----------------------------
        genomeElementProperties.put(GenomeElementNode.NODE_TYPE_PROPERTY, GenomeElementNode.NODE_TYPE);
        geneProperties.put(GeneNode.NODE_TYPE_PROPERTY, GeneNode.NODE_TYPE);
        cdsProperties.put(CDSNode.NODE_TYPE_PROPERTY, CDSNode.NODE_TYPE);
        miscRnaProperties.put(MiscRNANode.NODE_TYPE_PROPERTY, MiscRNANode.NODE_TYPE);
        mRnaProperties.put(MRNANode.NODE_TYPE_PROPERTY, MRNANode.NODE_TYPE);
        ncRnaProperties.put(NcRNANode.NODE_TYPE_PROPERTY, NcRNANode.NODE_TYPE);
        rRnaProperties.put(RRNANode.NODE_TYPE_PROPERTY, RRNANode.NODE_TYPE);
        tmRnaProperties.put(TmRNANode.NODE_TYPE_PROPERTY, TmRNANode.NODE_TYPE);
        tRnaProperties.put(TRNANode.NODE_TYPE_PROPERTY, TRNANode.NODE_TYPE);
        //----------------------------------------------------------------------------------
        //----------------------------------------------------------------------------------
        

        try {
            // This block configures the logger with handler and formatter
            fh = new FileHandler("ImportGenbank.log", false);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);

            // create the batch inserter
            inserter = new BatchInserterImpl(CommonData.DATABASE_FOLDER, BatchInserterImpl.loadProperties(CommonData.PROPERTIES_FILE_NAME));

            // create the batch index service
            indexProvider = new LuceneBatchInserterIndexProvider(inserter);


            //-----------------create batch indexes----------------------------------
            //----------------------------------------------------------------------
            BatchInserterIndex genomeElementVersionIndex = indexProvider.nodeIndex(GenomeElementNode.GENOME_ELEMENT_VERSION_INDEX,
                    MapUtil.stringMap(PROVIDER_ST, LUCENE_ST, TYPE_ST, EXACT_ST));

            for (File file : files) {
                if (file.getName().endsWith(".gbff")) {


                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line = null;

                    while ((line = reader.readLine()) != null) {

                        //this is the first line where the locus is
                        String accessionSt = "";
                        String definitionSt = "";
                        String versionSt = "";
                        String commentSt = "";
                        StringBuilder seqStBuilder = new StringBuilder();

                        ArrayList<String> cdsList = new ArrayList<String>();
                        ArrayList<String> geneList = new ArrayList<String>();
                        ArrayList<String> miscRnaList = new ArrayList<String>();
                        ArrayList<String> mRnaList = new ArrayList<String>();
                        ArrayList<String> ncRnaList = new ArrayList<String>();
                        ArrayList<String> rRnaList = new ArrayList<String>();
                        ArrayList<String> tmRnaList = new ArrayList<String>();
                        ArrayList<String> tRnaList = new ArrayList<String>();

                        boolean originFound = false;

                        //Now I get all the lines till I reach the string '//'
                        do {
                            boolean readLineFlag = true;

                            if (line.startsWith(GBCommon.LOCUS_STR)) {
                                // do nothing right now
                            } else if (line.startsWith(GBCommon.ACCESSION_STR)) {

                                accessionSt = line.split(GBCommon.ACCESSION_STR)[1].trim();

                            } else if (line.startsWith(GBCommon.VERSION_STR)) {

                                versionSt = line.split(GBCommon.VERSION_STR)[1].trim().split(" ")[0];

                            } else if (line.startsWith(GBCommon.DEFINITION_STR)) {

                                definitionSt += line.split(GBCommon.DEFINITION_STR)[1].trim();
                                do {
                                    line = reader.readLine();
                                    if (line.startsWith(" ")) {
                                        definitionSt += line.trim();
                                    }
                                } while (line.startsWith(" "));
                                readLineFlag = false;

                            } else if (line.startsWith(GBCommon.COMMENT_STR)) {

                                commentSt += line.split(GBCommon.COMMENT_STR)[1].trim();
                                do {
                                    line = reader.readLine();
                                    if (line.startsWith(" ")) {
                                        commentSt += "\n" + line.trim();
                                    }
                                } while (line.startsWith(" "));
                                readLineFlag = false;

                            } else if (line.startsWith(GBCommon.FEATURES_STR)) {


                                do {
                                    line = reader.readLine();

                                    if (line.trim().startsWith(GBCommon.CDS_STR)) {
                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.CDS_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        cdsList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.GENE_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.GENE_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        geneList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.MISC_RNA_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.MISC_RNA_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        miscRnaList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.TM_RNA_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.TM_RNA_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        tmRnaList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.R_RNA_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.R_RNA_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        rRnaList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.M_RNA_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.M_RNA_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        mRnaList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.NC_RNA_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.NC_RNA_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        ncRnaList.add(positionsSt);

                                    } else if (line.trim().startsWith(GBCommon.T_RNA_STR)) {

                                        String positionsSt = "";
                                        positionsSt += line.trim().split(GBCommon.T_RNA_STR)[1].trim();

                                        line = reader.readLine();

                                        while (!line.trim().startsWith("/")) {
                                            positionsSt += line.trim();
                                            line = reader.readLine();
                                        }

                                        tRnaList.add(positionsSt);

                                    }

                                } while (line.startsWith(" "));
                                readLineFlag = false;

                            } else if (line.startsWith(GBCommon.ORIGIN_STR)) {
                                //sequence

                                originFound = true;

                                do {
                                    line = reader.readLine();
                                    String[] tempArray = line.trim().split(" ");
                                    for (int i = 1; i < tempArray.length; i++) {
                                        seqStBuilder.append(tempArray[i]);
                                    }

                                } while (line.startsWith(" "));
                                readLineFlag = false;
                            }

                            if (readLineFlag) {
                                line = reader.readLine();
                            }



                        } while (line != null && !line.startsWith(GBCommon.LAST_LINE_STR));




                        //-----we only save the data when the sequence is found------------
                        if (originFound) {


                            System.out.println("accessionSt = " + accessionSt);
                            System.out.println("versionSt = " + versionSt);
                            System.out.println("definitionSt = " + definitionSt);
                            System.out.println("commentSt = " + commentSt);
                            System.out.println("sequence.length = " + seqStBuilder.toString().length());

                            System.out.println("geneList = " + geneList);
                            System.out.println("cdsList = " + cdsList);
                            System.out.println("miscRnaList = " + miscRnaList);
                            System.out.println("mRnaList = " + mRnaList);
                            System.out.println("ncRnaList = " + ncRnaList);
                            System.out.println("rRnaList = " + rRnaList);
                            System.out.println("tmRnaList = " + tmRnaList);
                            System.out.println("tRnaList = " + tRnaList);


                            //--------create genome element node--------------
                            long genomeElementId = createGenomeElementNode(versionSt,
                                    commentSt, definitionSt, inserter, genomeElementVersionIndex);

                            //-----------genes-----------------
                            for (String genePositionsSt : geneList) {                                
                                geneProperties.put(GeneNode.POSITIONS_PROPERTY, genePositionsSt);
                                long geneId = inserter.createNode(geneProperties);                                
                                inserter.createRelationship(genomeElementId, geneId, genomeElementGeneRel, null);                                
                            }
                            
                            //-----------CDS-----------------
                            for (String cdsPositionsSt : cdsList) {                                
                                cdsProperties.put(CDSNode.POSITIONS_PROPERTY, cdsPositionsSt);
                                long cdsID = inserter.createNode(cdsProperties);                                
                                inserter.createRelationship(genomeElementId, cdsID, genomeElementCDSRel, null);                                
                            }
                            
                            //-----------misc rna-----------------
                            for (String miscRnaPositionsSt : miscRnaList) {                                
                                miscRnaProperties.put(MiscRNANode.POSITIONS_PROPERTY, miscRnaPositionsSt);
                                long miscRnaID = inserter.createNode(miscRnaProperties);                                
                                inserter.createRelationship(genomeElementId, miscRnaID, genomeElementMiscRnaRel, null);                                
                            }
                            
                            //-----------m rna-----------------
                            for (String mRnaPositionsSt : mRnaList) {                                
                                mRnaProperties.put(MRNANode.POSITIONS_PROPERTY, mRnaPositionsSt);
                                long mRnaID = inserter.createNode(mRnaProperties);                                
                                inserter.createRelationship(genomeElementId, mRnaID, genomeElementMRnaRel, null);                                
                            }
                            
                            //-----------nc rna-----------------
                            for (String ncRnaPositionsSt : ncRnaList) {                                
                                ncRnaProperties.put(NcRNANode.POSITIONS_PROPERTY, ncRnaPositionsSt);
                                long ncRnaID = inserter.createNode(ncRnaProperties);                                
                                inserter.createRelationship(genomeElementId, ncRnaID, genomeElementNcRnaRel, null);                                
                            }
                            
                            //-----------r rna-----------------
                            for (String rRnaPositionsSt : rRnaList) {                                
                                rRnaProperties.put(RRNANode.POSITIONS_PROPERTY, rRnaPositionsSt);
                                long rRnaID = inserter.createNode(rRnaProperties);                                
                                inserter.createRelationship(genomeElementId, rRnaID, genomeElementRRnaRel, null);                                
                            }
                            
                            //-----------tm rna-----------------
                            for (String tmRnaPositionsSt : tmRnaList) {                                
                                tmRnaProperties.put(TmRNANode.POSITIONS_PROPERTY, tmRnaPositionsSt);
                                long tmRnaID = inserter.createNode(tmRnaProperties);                                
                                inserter.createRelationship(genomeElementId, tmRnaID, genomeElementTmRnaRel, null);                                
                            }
                            
                            //-----------t rna-----------------
                            for (String tRnaPositionsSt : tRnaList) {                                
                                tRnaProperties.put(TRNANode.POSITIONS_PROPERTY, tRnaPositionsSt);
                                long tRnaID = inserter.createNode(tRnaProperties);                                
                                inserter.createRelationship(genomeElementId, tRnaID, genomeElementTRnaRel, null);                                
                            }


                        }

                    }





                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement stackTraceElement : trace) {
                logger.log(Level.SEVERE, stackTraceElement.toString());
            }
        } finally {

            // shutdown, makes sure all changes are written to disk
            indexProvider.shutdown();
            inserter.shutdown();

            // closing logger file handler
            fh.close();

        }




    }

    private static void ftpStuff() {
        try {

            FTPClient ftp = new FTPClient();
            ftp.connect("ftp.ncbi.nih.gov");

            System.out.println(ftp.getReplyString());

            ftp.login("anonymous", "asdfjkjd83djsdf@gmail.com");

            System.out.println("before list files...");

            //ftp.li

            FTPFile[] files = ftp.listFiles(BASE_FOLDER);

            System.out.println(files.length);

            for (FTPFile file : files) {

                if (file.getName().endsWith(".gbff.gz")) {

                    StringWriter writer = null;
                    String charset = "ASCII";

                    GZIPInputStream inputStream = new GZIPInputStream(ftp.retrieveFileStream(BASE_FOLDER + "/" + file.getName()));

                    System.out.println("ftp.getControlEncoding() = " + ftp.getControlEncoding());

                    Reader decoder = new InputStreamReader(inputStream, charset);
                    BufferedReader buffered = new BufferedReader(decoder);

                    String line = null;

                    while ((line = buffered.readLine()) != null) {
                        System.out.println("line = " + line);
                    }

                    System.exit(0);
                }
            }



        } catch (Exception ex) {
            Logger.getLogger(ImportGenBank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static long createGenomeElementNode(String version,
            String comment,
            String definition,
            BatchInserter inserter,
            BatchInserterIndex index) {

        genomeElementProperties.put(GenomeElementNode.VERSION_PROPERTY, version);
        genomeElementProperties.put(GenomeElementNode.COMMENT_PROPERTY, comment);
        genomeElementProperties.put(GenomeElementNode.DEFINITION_PROPERTY, definition);

        long genomeElementId = inserter.createNode(genomeElementProperties);
        index.add(genomeElementId, MapUtil.map(GenomeElementNode.GENOME_ELEMENT_VERSION_INDEX, version));
        return genomeElementId;

    }
}