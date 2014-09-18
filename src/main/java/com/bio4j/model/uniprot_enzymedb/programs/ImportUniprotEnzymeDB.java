package com.bio4j.model.uniprot_enzymedb.programs;

import com.bio4j.model.enzymedb.nodes.Enzyme;
import com.bio4j.model.uniprot.nodes.Protein;
import com.bio4j.model.uniprot_enzymedb.UniprotEnzymeDBGraph;
import com.ohnosequences.typedGraphs.UntypedGraph;
import com.ohnosequences.xml.api.model.XMLElement;
import org.jdom2.Element;

import java.io.*;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author <a href="mailto:ppareja@era7.com">Pablo Pareja Tobes</a>
 */
public abstract class ImportUniprotEnzymeDB<I extends UntypedGraph<RV,RVT,RE,RET>,RV,RVT,RE,RET> {

	private static final Logger logger = Logger.getLogger("ImportUniprotEnzymeDB");
	private static FileHandler fh;

	protected abstract UniprotEnzymeDBGraph<I,RV,RVT,RE,RET> config(String dbFolder);

	public static final String ENTRY_TAG_NAME = "entry";
	public static final String ENTRY_ACCESSION_TAG_NAME = "accession";
	public static final String DB_REFERENCE_TAG_NAME = "dbReference";
	public static final String DB_REFERENCE_TYPE_ATTRIBUTE = "type";
	public static final String DB_REFERENCE_ID_ATTRIBUTE = "id";
	public static final String ENZYME_REFERENCE_TYPE = "EC";


	public void importUniprotEnzymeDB(String[] args) {

		if (args.length != 2) {
			System.out.println("This program expects the following parameters: \n"
					+ "1. Uniprot xml filename \n"
					+ "2. Bio4j DB folder \n");
		} else {

			long initTime = System.nanoTime();

			File inFile = new File(args[0]);
			String dbFolder = args[1];


			UniprotEnzymeDBGraph<I,RV,RVT,RE,RET> uniprotEnzymeDBGraph = config(dbFolder);

			BufferedWriter statsBuff = null;

			int proteinCounter = 0;
			int limitForPrintingOut = 10000;

			try {

				// This block configures the logger with handler and formatter
				fh = new FileHandler("ImportUniprotEnzymeDB" + args[0].split("\\.")[0] + ".log", false);

				SimpleFormatter formatter = new SimpleFormatter();
				fh.setFormatter(formatter);
				logger.addHandler(fh);
				logger.setLevel(Level.ALL);

				//---creating writer for stats file-----
				statsBuff = new BufferedWriter(new FileWriter(new File("ImportUniprotEnzymeDBStats_" + inFile.getName().split("\\.")[0] + ".txt")));

				BufferedReader reader = new BufferedReader(new FileReader(inFile));
				StringBuilder entryStBuilder = new StringBuilder();
				String line;

				while ((line = reader.readLine()) != null) {
					if (line.trim().startsWith("<" + ENTRY_TAG_NAME)) {

						while (!line.trim().startsWith("</" + ENTRY_TAG_NAME + ">")) {
							entryStBuilder.append(line);
							line = reader.readLine();
						}
						//linea final del organism
						entryStBuilder.append(line);
						//System.out.println("organismStBuilder.toString() = " + organismStBuilder.toString());
						XMLElement entryXMLElem = new XMLElement(entryStBuilder.toString());
						entryStBuilder.delete(0, entryStBuilder.length());

						String accessionSt = entryXMLElem.asJDomElement().getChildText(ENTRY_ACCESSION_TAG_NAME);

						Protein<I,RV,RVT,RE,RET> protein = null;

						//-----db references-------------
						List<Element> dbReferenceList = entryXMLElem.asJDomElement().getChildren(DB_REFERENCE_TAG_NAME);

						for (Element dbReferenceElem : dbReferenceList) {

							//-------------------GO -----------------------------
							if (dbReferenceElem.getAttributeValue(DB_REFERENCE_TYPE_ATTRIBUTE).toUpperCase().equals(ENZYME_REFERENCE_TYPE)) {

								if(protein == null){
									protein = uniprotEnzymeDBGraph.uniprotGraph().proteinAccessionIndex().getVertex(accessionSt).get();
								}


								String enzymeID = dbReferenceElem.getAttributeValue(DB_REFERENCE_ID_ATTRIBUTE);

								Enzyme<I,RV,RVT,RE,RET> enzyme = uniprotEnzymeDBGraph.enzymeDBGraph().enzymeIdIndex().getVertex(enzymeID).get();

								protein.addOutEdge(uniprotEnzymeDBGraph.EnzymaticActivity(), enzyme);


							}

						}
						//---------------------------------------------------------------------------------------


						proteinCounter++;
						if ((proteinCounter % limitForPrintingOut) == 0) {
							String countProteinsSt = proteinCounter + " proteins updated!!";
							logger.log(Level.INFO, countProteinsSt);
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

				try {
					//------closing writers-------

					// shutdown, makes sure all changes are written to disk
					uniprotEnzymeDBGraph.raw().shutdown();

					// closing logger file handler
					fh.close();

					//-----------------writing stats file---------------------
					long elapsedTime = System.nanoTime() - initTime;
					long elapsedSeconds = Math.round((elapsedTime / 1000000000.0));
					long hours = elapsedSeconds / 3600;
					long minutes = (elapsedSeconds % 3600) / 60;
					long seconds = (elapsedSeconds % 3600) % 60;

					statsBuff.write("Statistics for program ImportUniprot:\nInput file: " + inFile.getName()
							+ "\nThere were " + proteinCounter + " proteins inserted.\n"
							+ "The elapsed time was: " + hours + "h " + minutes + "m " + seconds + "s\n");

					//---closing stats writer---
					statsBuff.close();


				} catch (IOException ex) {
					Logger.getLogger(ImportUniprotEnzymeDB.class.getName()).log(Level.SEVERE, null, ex);
				}

			}
		}

	}

}