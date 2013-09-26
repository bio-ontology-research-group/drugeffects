def hpo = new File("human-phenotype-ontology.obo")

def umlsset = new TreeSet()
def id = ""
hpo.eachLine { line ->
  if (line.startsWith("id: ")) {
    id = line.substring(4).trim()
  }
  if (line.startsWith("xref: UMLS")) {
    def umls = line.substring(11)
    umls = umls.substring(0, umls.indexOf(" ")+1).trim()
    println "$id\t$umls"
    umlsset.add(umls)
  }
}

def mismatches = [:]
def matchset = new TreeSet()
def match = 0
def mismatch = 0
def umls2name = [:]
def sider = new File("indications_raw.tsv")
sider.splitEachLine("\t") { line ->
  umls2name[line[1]] = line[2]
  if (line[1] in umlsset) {
    match +=1
    matchset.add(line[1])
  } else {
    mismatch += 1
    if (mismatches[line[1]]!=null) {
      mismatches[line[1]] += 1
    } else {
      mismatches[line[1]] = 1
    }
  }
}

def mismatchlist = []
mismatches.each { key, value ->
  Expando exp = new Expando()
  exp.umls = key
  exp.name = umls2name[key]
  exp.count = value
  mismatchlist << exp
}

mismatchlist = mismatchlist.sort { exp -> exp.count }

println "Match: $match (size: "+matchset.size()+")"
println "Mismatch: $mismatch (size: "+mismatchset.size()+")"

mismatchlist.each { exp ->
  println exp.name+"\t"+exp.umls+"\t"+exp.count
}