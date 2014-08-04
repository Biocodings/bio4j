package com.bio4j.model.uniprot.relationships;

import com.bio4j.model.uniprot.UniprotGraph;
import com.bio4j.model.uniprot.nodes.CommentType;
import com.bio4j.model.uniprot.nodes.Protein;
import com.ohnosequences.typedGraphs.UntypedGraph;

/**
 * Created by ppareja on 7/28/2014.
 */
public final class ProteinComment <I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
		extends
		UniprotGraph.UniprotEdge<
				Protein<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.ProteinType,
				ProteinComment<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.ProteinCommentType,
				CommentType<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.CommentTypeType,
				I, RV, RVT, RE, RET
				> {

	public ProteinComment(RE edge, UniprotGraph<I, RV, RVT, RE, RET>.ProteinCommentType type) {

		super(edge, type);
	}

	@Override
	public ProteinComment<I, RV, RVT, RE, RET> self() {
		return this;
	}
}