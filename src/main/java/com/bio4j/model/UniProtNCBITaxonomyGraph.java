/*
  # UniProt NCBI taxonomy annotations

  There are two interesnting pieces of information here:

  1. the organism from which this protein comes
  2. the host, for viruses and the like
*/
package com.bio4j.model;

import com.bio4j.angulillos.*;
import com.bio4j.angulillos.Arity.*;

public final class UniProtNCBITaxonomyGraph<V,E> extends TypedGraph<UniProtNCBITaxonomyGraph<V,E>,V,E> {

  public UniProtNCBITaxonomyGraph(UniProtGraph<V,E> uniProtGraph, NCBITaxonomyGraph<V,E> ncbiTaxonomyGraph) {

    super(uniProtGraph.raw());
    this.uniProtGraph = uniProtGraph;
    this.ncbiTaxonomyGraph = ncbiTaxonomyGraph;
  }

  public final UniProtGraph<V,E> uniProtGraph;
  public final NCBITaxonomyGraph<V,E> ncbiTaxonomyGraph;

  @Override public final UniProtNCBITaxonomyGraph<V,E> self() { return this; }

  // TODO add edges here: organism which is toOne, and host about which I'm not sure
}