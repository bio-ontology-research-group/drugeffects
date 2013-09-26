def drug2indication = [:]

def name2id = [:] // omim name 2 omim id
new File("evaluation/omim-umls-mapping.tsv.csv").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1]
  name2id[name] = id
}

def flag = false
new File("evaluation/indications.tsv.csv").splitEachLine("\t") { line ->
  if (!flag) { flag = true } else {
    def drug = line[0]
    def dis = line[1]
    def id = name2id[dis]
    if (id!=null) {
      if (drug2indication[drug] == null) {
	drug2indication[drug] = new TreeSet()
      }
      drug2indication[drug].add(id)
    }
  }
}

def name2stitch = [:]
new File("stitchphenotypes-names.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1].trim().toLowerCase()
  if (name2stitch[name] == null) {
    name2stitch[name] = new TreeSet()
  }
  name2stitch[name].add(id)
}

drug2indication.each { drug, omim ->
  drug = drug.toLowerCase()
  if (name2stitch[drug]) {
    name2stitch[drug].each { stitch ->
      omim.each { o ->
	println "$stitch\tOMIM:$o"
      }
    }
  }
}