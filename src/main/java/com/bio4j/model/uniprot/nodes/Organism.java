package com.bio4j.model.uniprot.nodes;

import com.bio4j.model.uniprot.UniprotGraph;
import com.bio4j.model.uniprot.relationships.OrganismTaxon;
import com.bio4j.model.uniprot.relationships.ProteinOrganism;
import com.ohnosequences.typedGraphs.UntypedGraph;

import java.util.List;

/**
 * Created by ppareja on 7/23/2014.
 */
public final class Organism <I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
		extends UniprotGraph.UniprotVertex<
		Organism<I, RV, RVT, RE, RET>,
		UniprotGraph<I, RV, RVT, RE, RET>.OrganismType,
		I, RV, RVT, RE, RET
		> {

	public Organism(RV vertex, UniprotGraph<I, RV, RVT, RE, RET>.OrganismType type) {
		super(vertex, type);
	}

	@Override
	public Organism<I, RV, RVT, RE, RET> self() {
		return this;
	}

	// properties
	public String scientificName() {
		return get(type().scientificName);
	}
	public String commonName() {
		return get(type().commonName);
	}
	public String synonymName() {
		return get(type().synonymName);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////

	// relationships

	// organismTaxon
	// outgoing
	public OrganismTaxon<I, RV, RVT, RE, RET> organismTaxon_out(){
		return inMany(graph().OrganismTaxon());
	}
	public Taxon<I, RV, RVT, RE, RET> organismTaxon_outNode(){
		return inManyV(graph().OrganismTaxon());
	}

	// proteinOrganism
	// ingoing
	public List<ProteinOrganism<I, RV, RVT, RE, RET>> proteinOrganism_in(){
		return inMany(graph().ProteinOrganism());
	}
	public List<Protein<I, RV, RVT, RE, RET>> proteinOrganism_inNodes(){
		return inManyV(graph().ProteinOrganism());
	}


}