/* Generates drug effects for SIDER drugs from mouse phenotypes */


def fout = new PrintWriter(new BufferedWriter(new FileWriter("sider-effects.txt")))

def orthologyf = new File("mgi/HMD_HumanSequence.rpt")
def orthology = [:] // human gene -> mouse gene (using marker ID for mouse, symbols for human)
orthologyf.splitEachLine("\t") { line ->
  def mg = line[1].trim()
  def hg = line[3].trim()
  orthology[hg] = mg
}


def targets = [:] // drug to targets that are inhibited (using human gene symbol)
def inhibitors = [:] // drug to targets that are inhibited (using drugbank IDs for targets)
def chebi2drugbank = [:]
def drugbankfile = new File("drugbank/drugbank.xml")
def slurper = new XmlSlurper().parse(drugbankfile)

slurper.drug.each { drug ->
  def did = drug."drugbank-id".text()
  drug.targets.target.each { target ->
    def tid = target.@partner.text()
    def inhibitor = false
    target.actions.action.each { action ->
      if (action.text() == "inhibitor") {
	inhibitor = true
      }
    }
    if (inhibitor) {
      if (inhibitors[did]==null) {
	inhibitors[did] = new TreeSet()
      }
      inhibitors[did].add(tid)
    }
  }
  drug."external-identifiers"."external-identifier".each { ext ->
    def resource = ext."resource".text()
    def eid = ext."identifier".text()
    if (resource == "ChEBI") {
      chebi2drugbank["CHEBI:"+eid] = did
    }
  }
}
def pid2genename = [:]
slurper.partners.partner.each { partner ->
  def pid = partner.@id.text()
  def gn = partner."gene-name".text()
  pid2genename[pid] = gn
}
inhibitors.each { drug, partners ->
  partners = partners.collect { pid2genename[it] }
  targets[drug] = partners
}

def mousetargets = [:]
targets.each { drug, target ->
  target = target.collect { orthology[it] }
  mousetargets[drug] = target
}


def phenotypefile = new File("phenotypes-sider.txt")
def phenotypes = [:]
phenotypefile.splitEachLine("\t") { line ->
  if (line.size()>1) {
    line = line.collect { it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","") }
    def id = line[0]
    phenotypes[id] = line[1..-1]
    phenotypes[id] = phenotypes[id].collect { it.replaceAll("_",":") }
  }
}

def drugeffects = [:] // drugbank id to MP ids
mousetargets.each { drug, target ->
  drugeffects[drug] = new TreeSet()
  target.each { t ->
    if (t!=null) {
      def effects = phenotypes[t]
      if (effects!=null) {
	drugeffects[drug].addAll(effects)
      }
    }
  }
}

def sider2id = [:]
new File("id2siderlabel.txt").splitEachLine("\t") { line ->
  sider2id[line[0].trim()] = line[1]
}

def sider2stitch = [:]
new File("label_mapping.tsv").splitEachLine("\t") { line ->
  def label = line[-1]
  def stitch = line[3].replaceAll("-","")
  sider2stitch[label] = stitch
}
def stitch2chebi = [:]
new File("stitch/chemical.sources.v3.1.tsv").splitEachLine("\t") { line ->
  def id = line[2]
  def sid = line[0].replaceAll("CID","")
  if (id == "ChEBI") {
    stitch2chebi[sid] = line[3]
  }
}
def drugbank2sider = [:]
sider2stitch.each { sider, stitch ->
  drugbank2sider[chebi2drugbank[stitch2chebi[stitch]]] = sider
}


def sider2effects = [:]
drugbank2sider.each { drugbank, sider ->
  def sid = sider2id[sider]
  def effects = drugeffects[drugbank]
  println "$drugbank\t$sid\t$effects"
  sider2effects[sid] = effects
}
sider2effects.each { sid, effects ->
  fout.print("$sid\t")
  effects.each { fout.print("$it\t") }
  fout.println("")
}
fout.flush()
fout.close()
