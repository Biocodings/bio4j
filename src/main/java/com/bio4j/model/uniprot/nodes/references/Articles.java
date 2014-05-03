package com.bio4j.model.uniprot.nodes.references;

import java.util.List;

import com.bio4j.model.Node;
import com.bio4j.model.NodeType;

import com.bio4j.model.uniprot.relationships.references.Article;

/**
 *  This Node has just one instance. A `Reference` node is an article if it has a relationship of type `Article` pointing to the unique `Articles` node.
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 * @author Eduardo Pareja-Tobes <eparejatobes@ohnosequences.com>
 */
public interface Articles extends Node<Articles, Articles.Type> {
  
  public List<? extends Article> article_in();
  public List<? extends Reference> article_inNodes();

  public static Type TYPE = Type.articles;

  public static enum Type implements NodeType<Articles, Articles.Type> {
    
    articles;
    
    public Type value() { return articles; }
  }
}