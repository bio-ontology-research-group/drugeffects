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

// find all MP terms affecting morphology
def tcandidates = new TreeSet()
def oid = ""
new File("mp-xp.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    oid = line.substring(3).trim()
    if (oid.indexOf("!")>-1) {
      oid = oid.substring(0,oid.indexOf("!")-1).trim()
    }
    if (line.indexOf("development")>-1) {
      tcandidates.add(oid)
    }
  }
}

def tetralogTerms = new TreeSet()
ont.getClassesInSignature().each { cl ->
  def clst = cl.toString()
  if (clst.indexOf("0005380")>-1 ) { // || clearNames(clst) in tcandidates) {
    def s = reasoner.getSubClasses(cl, false).getFlattened()
    def t = reasoner.getEquivalentClasses(cl).getEntities()
    s.addAll(t)
    s.each {
      tetralogTerms.add(clearNames(it.toString()))
    }
  }
}

def tetralogCompounds = new TreeSet()
new File("evaluation/teratologs.txt").splitEachLine("\t") { line ->
  if (line[-1].indexOf("MP")) { 
    tetralogCompounds.add(line[-1])
  }
}

def stitchinsider = new TreeSet()
new File("stitchphenotypes-names.txt").splitEachLine("\t") { line ->
  def sid = line[0]
  if (sid.startsWith("STITCHMP:")) {
    stitchinsider.add(sid)
  }
}

def stitch2mp = [:]
new File("stitch-effects.txt").splitEachLine("\t") { line ->
  def sid = line[0].replaceAll("CID","STITCHMP:")
  def mp = line[1]
  if (sid in stitchinsider) {
    if (stitch2mp[sid] == null) {
      stitch2mp[sid] = new TreeSet()
    }
    stitch2mp[sid].add(mp)
  }
}

def fp = 0
def tp = 0
def fn = 0
def tn = 0

def fpset = new TreeSet()
stitch2mp.each { stitch, phenos ->
  if (phenos.intersect(tetralogTerms).size()>0) {
    if (stitch in tetralogCompounds) {
      tp += 1
    } else {
      fp += 1
      fpset.add(stitch)
    }
  } else {
    if (stitch in tetralogCompounds) {
      fn += 1
    } else {
      tn += 1
    }
  }
}

println "TN: $tn\nTP: $tp\nFN: $fn\nFP: $fp"

println fpset

System.exit(0)
