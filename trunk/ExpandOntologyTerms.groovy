import org.semanticweb.owlapi.io.* 
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.elk.owlapi.*

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("mammalian_phenotype.obo"))

OWLDataFactory fac = manager.getOWLDataFactory()

OWLReasonerFactory reasonerFactory = null

OWLReasonerFactory fac1 = new ElkReasonerFactory()
OWLReasoner reasoner = fac1.createReasoner(ont)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def clearNames = { s ->
  s.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
}

def mp2mp = [:]
ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  def s = reasoner.getSuperClasses(cl, false).getFlattened()
  def t = reasoner.getEquivalentClasses(cl).getEntities()
  s.addAll(t)
  mp2mp[clst] = new TreeSet()
  s.each { mp2mp[clst].add(it.toString()) }
}

def hp2mp = [:]
new File("phenomenetdata/hp2mp.txt").splitEachLine("\t") { line ->
  if (line.size()>1) {
    def hp = line[0]
    hp2mp[hp] = new TreeSet()
    line[1..-1].each { hp2mp[hp].add(it) }
  }
}

mp2mp.each { key, val ->
  print clearNames(key)+"\t"
  val.each { v ->
    print clearNames(v)+"\t"
  }
  println ""
}
hp2mp.each { key, val ->
  print clearNames(key)+"\t"
  val.each { v ->
    print clearNames(v)+"\t"
  }
  println ""
}

System.exit(0)