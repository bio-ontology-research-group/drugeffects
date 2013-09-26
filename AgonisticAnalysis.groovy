import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.profiles.*
import org.semanticweb.owlapi.util.*
import org.semanticweb.owlapi.io.*
import org.semanticweb.elk.owlapi.*

/* get superclasses for PATO terms */

OWLOntologyManager manager = OWLManager.createOWLOntologyManager()

OWLDataFactory fac = manager.getOWLDataFactory()
def factory = fac

OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("quality.obo"))

OWLReasonerFactory reasonerFactory = null

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)

OWLReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(ont,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)

def id2super = [:]
ont.getClassesInSignature().each { cl ->
  def clst = cl.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
  if (id2super[clst] == null) {
    id2super[clst] = new TreeSet()
  }
  id2super[clst].add(clst)
  reasoner.getSuperClasses(cl, false).getFlattened().each { sup ->
    def supst = sup.toString().replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")
    id2super[clst].add(supst)
  }
}

def stitch2omim = [:]
new File("evaluation/stitch-indications.txt").splitEachLine("\t") { line ->
  def sid = line[0]
  def dis = line[1]
  if (stitch2omim[sid] == null) {
    stitch2omim[sid] = new TreeSet()
  }
  stitch2omim[sid].add(dis)
}

/* Find directionality of phenotypes as asserted in PATO */

def incmap = [:]
def decmap = [:]

def id = ""
def sup = ""
new File("quality.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("intersection_of: PATO:")) {
    sup = line.substring(17).trim()
    sup = sup.substring(0, sup.indexOf(" "))
  }
  if (line.startsWith("intersection_of: increased_in_magnitude_relative_to PATO:0000461")) {
    if (incmap[id] == null) {
      incmap[id] = new TreeSet()
    }
    incmap[id].add(sup)
  }
  if (line.startsWith("intersection_of: decreased_in_magnitude_relative_to PATO:0000461")) {
    if (decmap[id] == null) {
      decmap[id] = new TreeSet()
    }
    decmap[id].add(sup)
  }
}

id2super.each { tid, supt ->
  supt.each { sid ->
    if (decmap[sid]) {
      if (decmap[tid] == null) {
	decmap[tid] = new TreeSet()
      }
      decmap[tid].addAll(decmap[sid])
    }
    if (incmap[sid]) {
      if (incmap[tid] == null) {
	incmap[tid] = new TreeSet()
      }
      incmap[tid].addAll(incmap[sid])
    }
  }
}
reasoner.dispose()

/* now read phenotypes */

def phenomap = [:] // id to set of MP terms

new File("stitchmp-phenotypes.txt").splitEachLine("\t") { line ->
  def i = line[0]
  phenomap[i] = new TreeSet()
  line[1..-1].each { phenomap[i].add(it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","").replaceAll("_",":")) }
}

/* now parse MP-XP file to find EQ terms */

def mpid2eq = [:]
Expando exp = null
id = null
def e = null
def q = ""
new File("mp-xp.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    if (id) {
      exp.e = e
      exp.q = q
      mpid2eq[id] = exp
    }
    id = line.substring(3).trim()
    if (id.indexOf("!")>-1) {
      id = id.substring(0, id.indexOf("!")).trim()
    }
    exp = new Expando()
    e = new TreeSet()
    q = ""
  }
  if (line.startsWith("intersection_of: PATO:")) {
    def qid = line.substring(17)
    qid = qid.substring(0, qid.indexOf(" ")).trim()
    q = qid
  }
  if (line.startsWith("intersection_of: inheres_in ")) {
    def eid = line.substring(28)
    if (eid.indexOf("!")>-1) {
      eid = eid.substring(0, eid.indexOf(" ")).trim()
    }
    e.add(eid)
  }
}

def pheno2direction = [:] // entity id mapped to set of E plus Trait plus direction

phenomap.each { i, pheno ->
  id = i
  if (pheno2direction[id] == null) {
    pheno2direction[id] = new LinkedHashSet()
  }
  pheno.each { p ->
    exp = mpid2eq[p]
    if (exp) {
      def inc = incmap[exp.q]
      def dec = decmap[exp.q]
      if (inc) {
	exp.e.each { ent ->
	  inc.each {
	    Expando exp2 = new Expando()
	    exp2.entity = ent
	    exp2.trait = it
	    exp2.direction = 1
	    pheno2direction[id].add(exp2)
	  }
	}
      }
      if (dec) {
	exp.e.each { ent ->
	  dec.each {
	    Expando exp2 = new Expando()
	    exp2.entity = ent
	    exp2.trait = it
	    exp2.direction = -1
	    pheno2direction[id].add(exp2)
	  }
	}
      }
    }
  }
}

/* eliminate entries that have opposite directionality attached */
pheno2direction.each { i, dirs ->
  dirs.each { d1 ->
    dirs.each { d2 ->
      if (d1!=d2 && d1.trait == d2.trait && d1.entity == d2.entity && (d1.direction+d2.direction == 0)) {
	d1.direction = 0
	d2.direction = 0
      }
    }
  }
}

/* now calculate which drugs have effects opposite the direction to diseases (treat some symptoms */

pheno2direction.each { i1, dirs1 ->
  pheno2direction.each { i2, dirs2 ->
    if (i1.startsWith("STITCH") && i2.startsWith("OMIM")) {
      def makebetter = 0
      def makeworse = 0
      dirs1.each { d1 ->
	dirs2.each { d2 ->
	  if (d1.trait == d2.trait && d1.entity == d2.entity && d1.direction!=0 && (d1.direction+d2.direction == 0)) {
	    makebetter += 1
	  }
	  if (d1.trait == d2.trait && d1.entity == d2.entity && d1.direction!=0 && (Math.abs(d1.direction+d2.direction) == 2)) {
	    makeworse += 1
	  }
	}
      }
      //      if (makebetter > makeworse) {
      if (i2 in stitch2omim[i1]) {
	println "$i1\t$i2\t$makebetter\t$makeworse"
      }
    }
  }
}
