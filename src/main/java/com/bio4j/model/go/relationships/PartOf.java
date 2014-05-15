package com.bio4j.model.go.relationships;

import com.ohnosequences.typedGraphs.Relationship;
import com.ohnosequences.typedGraphs.RelationshipType;


import com.bio4j.model.go.nodes.Term;

/**
 *
 * @author <a href="mailto:ppareja@era7.com">Pablo Pareja Tobes</a>
 * @author <a href="mailto:eparejatobes@ohnosequences.com">Eduardo Pareja-Tobes</a>
 */
public interface PartOf extends Relationship <
  Term, Term.Type,
  PartOf,  PartOf.Type,
  Term, Term.Type
>
{

  @Override public Term source();
  @Override public Term target();

  public static Type TYPE = Type.partOfGo;
  
  @Override public default Type type() { return TYPE; }
  
  public static enum Type implements RelationshipType.ManyToMany <
    Term, Term.Type,
    PartOf,  PartOf.Type,
    Term, Term.Type
  >
  {

    partOfGo;

    public Type value() { return partOfGo; }
    public Term.Type sourceType() { return Term.TYPE; }
    public Term.Type targetType() { return Term.TYPE; }
  }
}
