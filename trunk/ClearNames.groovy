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

infile.splitEachLine("\t") { line ->
  line.each {
    if (id2name[it]!=null) {
      print id2name[it]+"\t"
    } else {
      print it+"\t"
    }
  }
  println ""
}