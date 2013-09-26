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

def umls2name = [:]
def missing = [:]
new File("offsides/offsides.tsv").splitEachLine("\t") { line ->
  def stitchid = line[0].replaceAll("\"","").replaceAll("CID","").trim()
  def umlsid = line[2].replaceAll("\"","").trim()
  def umlsname = line[3].replaceAll("\"","").trim()
  umls2name[umlsid] = umlsname
  if (umls2ont[umlsid] != null) {
    umls2ont[umlsid].each { ont ->
      println "$stitchid\t$ont\t$umlsname"
    }
  } else {
    if (!missing[umlsid]) {
      missing[umlsid] = 1
    } else {
      missing[umlsid] += 1
    }
  }
}

missing.each { key, val ->
  def n = umls2name[key]
  //  println "$key\t$n\t$val"
}