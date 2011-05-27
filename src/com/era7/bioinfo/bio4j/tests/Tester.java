/*
 * Copyright (C) 2010-2011  "Bio4j"
 *
 * This file is part of Bio4j
 *
 * Bio4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.era7.bioinfo.bio4j.tests;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.bio4j.CommonData;
import com.era7.bioinfo.bio4jmodel.nodes.GoTermNode;
import com.era7.bioinfo.bio4jmodel.relationships.go.IsAGoRel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.BatchInserterIndex;
import org.neo4j.graphdb.index.BatchInserterIndexProvider;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.impl.lucene.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.batchinsert.BatchInserter;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class Tester {

    public static final String PROPERTIES_FILE_NAME = "batchInserter.properties";
    private static final Logger logger = Logger.getLogger("Tester");
    private static Bio4jManager manager = null;

    public static void main(String[] args) throws IOException {

        //--------creating amazon s3 client--------------
        AmazonS3Client amazonS3Client = new AmazonS3Client(new PropertiesCredentials(new File("AwsCredentials.properties")));
        //Owner bucketOwner = amazonS3Client.getBucketAcl(CommonData.REFSEQ_BUCKET_NAME).getOwner();
        //--------------------------------

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(CommonData.REFSEQ_BUCKET_NAME);
        listObjectsRequest.setMaxKeys(10000000);
        ObjectListing objectListing = amazonS3Client.listObjects(listObjectsRequest);
        List<S3ObjectSummary> list = null;

        int counter = 0;
        
        do {
            list = objectListing.getObjectSummaries();

            for (S3ObjectSummary s3ObjectSummary : list) {
                amazonS3Client.deleteObject(CommonData.REFSEQ_BUCKET_NAME, s3ObjectSummary.getKey());
                counter++;
            }
            
            System.out.println(counter + " objects deleted");

        } while (list.size() > 0);





//        BatchInserter inserter = null;
//        BatchInserterIndexProvider indexProvider = null;
//        BatchInserterIndex goParentRelIndex, goTermIdIndex = null;
//
//
//        // create the batch inserter
//        inserter = new BatchInserterImpl(CommonData.DATABASE_FOLDER, BatchInserterImpl.loadProperties(PROPERTIES_FILE_NAME));
//
//
//        // create the batch index service
//        indexProvider = new LuceneBatchInserterIndexProvider(inserter);
//        Map<String, String> indexProps = MapUtil.stringMap("provider", "lucene", "type", "exact");
//        goTermIdIndex = indexProvider.nodeIndex( GoTermNode.GO_TERM_ID_INDEX, indexProps);
//        goParentRelIndex = indexProvider.relationshipIndex(IsAGoRel.IS_A_REL_INDEX, indexProps);
//
//        long node0Id = inserter.createNode(MapUtil.map("id","GO:0"));
//        goTermIdIndex.add(node0Id, MapUtil.map(GoTermNode.GO_TERM_ID_INDEX, "GO:0"));
//
//        long node1Id = inserter.createNode(MapUtil.map("id","GO:1"));
//        goTermIdIndex.add(node1Id, MapUtil.map(GoTermNode.GO_TERM_ID_INDEX, "GO:1"));
//
//        long relId = inserter.createRelationship(node1Id, node0Id, new IsAGoRel(null), null);
//        goParentRelIndex.add(relId, MapUtil.map(IsAGoRel.IS_A_REL_INDEX, String.valueOf(node1Id)));
//        
//        indexProvider.shutdown();
//        inserter.shutdown();
//
//        System.out.println("creating manager...");
//        manager = new Bio4jManager(CommonData.DATABASE_FOLDER);
//
//        long number = ((EmbeddedGraphDatabase) manager.getGraphService()).getConfig().getGraphDbModule().getNodeManager().getNumberOfIdsInUse(Node.class);
//        System.out.println("nodes number = " + number);
//
//        number = ((EmbeddedGraphDatabase) manager.getGraphService()).getConfig().getGraphDbModule().getNodeManager().getNumberOfIdsInUse(Relationship.class);
//        System.out.println("relationships number = " + number);
//
//        System.out.println("getting node...");
//        Transaction txn = manager.beginTransaction();
//
//
//
//        try {
//
//            Node node1 = manager.getGoTermIdIndex().get(GoTermNode.GO_TERM_ID_INDEX, "GO:1").getSingle();
//
//            System.out.println(node1.getProperty("id"));
//
//            IndexHits<Relationship> hits = manager.getIsAGoRelIndex().get(IsAGoRel.IS_A_REL_INDEX, String.valueOf(node1.getId()), null, null);
//            System.out.println("hits.size() = " + hits.size());
//
//            txn.success();
//
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, e.getMessage());
//            StackTraceElement[] trace = e.getStackTrace();
//            for (StackTraceElement stackTraceElement : trace) {
//                logger.log(Level.SEVERE, stackTraceElement.toString());
//            }
//        } finally {
//            try {
//                txn.finish();
//                manager.shutDown();
//            } catch (Exception e) {
//                logger.log(Level.SEVERE, e.getMessage());
//                StackTraceElement[] trace = e.getStackTrace();
//                for (StackTraceElement stackTraceElement : trace) {
//                    logger.log(Level.SEVERE, stackTraceElement.toString());
//                }
//            }
//
//        }

    }
}