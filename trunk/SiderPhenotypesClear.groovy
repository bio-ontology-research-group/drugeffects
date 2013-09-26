def infile = new File(args[0])

def id2name = [:]
def id = ""
new File("ontologies/mammalian_phenotype.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(4).trim()
  }
  if (line.startsWith("name:")) {
    def name = line.substring(5).trim()
    id2name[id] = name
  }
}

new File("ontologies/human-phenotype-ontology.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(4).trim()
  }
  if (line.startsWith("name:")) {
    def name = line.substring(5).trim()
    id2name[id] = name
  }
}

def sider2label = [:]
new File("id2siderlabel.txt").splitEachLine("\t") { line ->
  def s = line[0]
  def i = line[1]
  sider2label[i] = s
}


def label2name = [:]
new File("label_mapping.tsv").splitEachLine("\t") { line ->
  def l = line[-1]
  def name = line[1]
  label2name[l] = name
}

infile.splitEachLine("\t") { line ->
  def lab = line[0]
  def i = line[1]
  i = id2name[i]
  lab = label2name[sider2label[lab]]
  println "$lab\t$i"
}