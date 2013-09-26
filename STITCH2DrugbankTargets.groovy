

/* Selects human targets for drugs from DrugBank (mapped to STITCH identifiers) as positive */


def targets = [:]
def drugbank2target = [:]
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
      if (drugbank2target[did]==null) {
	drugbank2target[did] = new TreeSet()
      }
      drugbank2target[did].add(tid)
    }
  }
}
def pid2uniprot = [:]
slurper.partners.partner.each { partner ->
  def pid = partner.@id.text()
  partner."external-identifiers"."external-identifier".each { ext ->
    if (ext.resource == "UniProtKB") {
      pid2uniprot[pid] = ext.identifier.text()
    }
  }
}
drugbank2target.each { drug, partners ->
  partners = partners.collect { pid2uniprot[it] }
  targets[drug] = partners
}

def drugbank2stitch = [:]
new File("stitch/chemical.aliases.v3.1.tsv").splitEachLine("\t") { line ->
  if (line[2] == "DrugBank") {
    def sid = line[0]
    def did = line[1]
    drugbank2stitch[did] = sid
  }
}

def uniprot2cid = [:] // homologeneclusterid
new File("mgi/HOM_MouseHumanSequence.rpt").splitEachLine("\t") { line ->
  def cid = line[0]
  def uid = line[-1]
  if (line[1] == "human") {
    uid.split(",").each { u ->
      uniprot2cid[u] = cid
    }
  }
}
def cid2mgi = [:]
new File("mgi/HMD_HumanPhenotype.rpt").splitEachLine("\t") { line ->
  def cid = line[2]
  def mgi = line[4]
  cid2mgi[cid] = mgi
}


targets.each { db, uni ->
  def sid = drugbank2stitch[db]
  if (sid) {
    def stitch = sid.replaceAll("CID","")
    uni.each { u ->
      def cid = uniprot2cid[u]
      if (cid) {
	def mgi = cid2mgi[cid]
	if (mgi) {
	  println "STITCHORIG:$stitch\t$mgi"
	  println "STITCHMP:$stitch\t$mgi"
	  println "STITCHCOMBINED:$stitch\t$mgi"
	}
      }
    }
  }
}