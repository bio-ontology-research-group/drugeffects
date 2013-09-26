
/* Represents SIDER drug effects using the MP */

def umls2ont = [:]
new File("sider-mp-mappings.tsv").splitEachLine("\t") { line ->
  def umls = line[1]
  def ontterm = line[3]?.trim()?.replaceAll("\"","")?.replaceAll("_",":")
  if (ontterm) {
    if (umls2ont[umls] == null) {
      umls2ont[umls] = new TreeSet()
    }
    umls2ont[umls].add(ontterm)
  }
}

new File("lexicalmapping.txt").splitEachLine("\t") { line ->
  def ontterm = line[0]
  def umls = line[1]
  if (umls2ont[umls] == null) {
    umls2ont[umls] = new TreeSet()
  }
  umls2ont[umls].add(ontterm)
}

new File("lexicalmapping-mp.txt").splitEachLine("\t") { line ->
  def ontterm = line[0]
  def umls = line[1]
  if (umls2ont[umls] == null) {
    umls2ont[umls] = new TreeSet()
  }
  umls2ont[umls].add(ontterm)
}

def id = ""
new File("human-phenotype-ontology.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("xref: UMLS:")) {
    def umls = line.substring(11).trim()
    if (umls.indexOf(" ")>-1) {
      umls = umls.substring(0,umls.indexOf(" ")).trim()
    }
    if (umls2ont[umls] == null) {
      umls2ont[umls] = new TreeSet()
    }
    umls2ont[umls].add(id)
  }
}

def sider2stitch = [:]
NumberFormat format = NumberFormat.getInstance()
format.setMinimumIntegerDigits(9)
new File("label_mapping.tsv").splitEachLine("\t") { line ->
  def siderid = line[-1]
  def stitchid = line[4]?.replaceAll("-","").trim()
  if (stitchid) {
    stitchid = new Integer(stitchid)
    stitchid = format.format(stitchid).replaceAll(",","")
    sider2stitch[siderid] = stitchid
  }
}

def stitch2ont = [:]
new File("meddra_adverse_effects.tsv").splitEachLine("\t") { line ->
  def stitchid = line[0].replaceAll("-","")
  def umlsid = line[6]
  def clearname = line[7]
  if (stitch2ont[stitchid] == null) {
    stitch2ont[stitchid] = new TreeSet()
  }
  stitch2ont[stitchid].add(umlsid)
}

new File("adverse_effects_raw.tsv").splitEachLine("\t") { line ->
  def siderid = line[0]
  def umlsid = line[1]
  def clearname = line[2]
  def stitchid = ""
  if (sider2stitch[siderid]!=null) {
    stitchid = sider2stitch[siderid]
  }
  if (umls2ont[umlsid]!=null) {
    umls2ont[umlsid].each { ontid ->
      println "$siderid\t$stitchid\t$umlsid\t$ontid\t$clearname"
    }
  }
}
sider2stitch.each { sider, stitch ->
  if (stitch2ont[stitch] != null) {
    stitch2ont[stitch].each { ont ->
      if (umls2ont[ont] != null) {
	umls2ont[ont].each { ot ->
	  println "$sider\t$stitch\t$ont\t$ot\t"
	}
      }
    }
  }
}
