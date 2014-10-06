package com.bio4j.model.uniprot.edges;

import com.bio4j.model.uniprot.UniprotGraph;
import com.bio4j.model.uniprot.vertices.Article;
import com.bio4j.model.uniprot.vertices.Reference;
import com.ohnosequences.typedGraphs.UntypedGraph;

/**
 * Created by ppareja on 7/28/2014.
 */
public final class ReferenceArticle <I extends UntypedGraph<RV, RVT, RE, RET>, RV, RVT, RE, RET>
		extends
		UniprotGraph.UniprotEdge<
				Reference<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.ReferenceType,
				ReferenceArticle<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.ReferenceArticleType,
				Article<I, RV, RVT, RE, RET>, UniprotGraph<I, RV, RVT, RE, RET>.ArticleType,
				I, RV, RVT, RE, RET
				> {

	public ReferenceArticle(RE edge, UniprotGraph<I, RV, RVT, RE, RET>.ReferenceArticleType type) {

		super(edge, type);
	}

	@Override
	public ReferenceArticle<I, RV, RVT, RE, RET> self() {
		return this;
	}
}