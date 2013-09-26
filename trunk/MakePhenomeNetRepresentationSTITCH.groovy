import java.net.*

PrintWriter fout = new PrintWriter(new FileWriter("stitchphenotypes-names.txt"))

def stitch2phenotype = [:]
def stitch2name = [:]

new File("endresults/sider-ontology-representation.txt").splitEachLine("\t") { line ->
  def stitchid = line[1].trim()
  def ontid = line[3]
  if (stitch2phenotype[stitchid] == null) {
    stitch2phenotype[stitchid] = new TreeSet()
  }
  stitch2phenotype[stitchid].add(ontid)
}

new File("stitch-effects.txt").splitEachLine("\t") { line ->
  def stitchid = line[0]?.replaceAll("CID","")
  def ontid = line[1]
  if (stitchid.startsWith("1")) {
    if (stitch2phenotype[stitchid] == null) {
      stitch2phenotype[stitchid] = new TreeSet()
    }
    stitch2phenotype[stitchid].add(ontid)
  }
}

new File("stitch/chemicals.v3.1.tsv").splitEachLine("\t") { line ->
  def  id = line[0].replaceAll("CID","")
  def name = line[1]
  stitch2name[id] = new TreeSet()
  stitch2name[id].add(name)
}
println stitch2name
