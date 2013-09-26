def hpo = new File("mammalian_phenotype.obo")

def name2id = [:]
def id = ""
hpo.eachLine { line ->
  if (line.startsWith("id: ")) {
    id = line.substring(4).trim()
  }
  if (line.startsWith("name: ")) {
    def name = line.substring(6).toLowerCase().trim()
    name2id[name] = id
  }
}

def umlsname2id = [:]
def siderf = new File("indications_raw.tsv")
siderf.splitEachLine("\t") { line ->
  id = line[1]
  def name = line[2].toLowerCase().trim()
  umlsname2id[name] = id
}

def umlsnameset = umlsname2id.keySet()
name2id.keySet().each { name ->
  if (name in umlsnameset) {
    println name2id[name]+"\t"+umlsname2id[name]
  }
}
