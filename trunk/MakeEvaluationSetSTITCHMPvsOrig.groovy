new File("stitchphenotypes.txt").splitEachLine("\t") { line ->
  def id = line[0]
  if (id.startsWith("STITCHMP")) {
    def id2 = id.replaceAll("MP","ORIG")
    println "$id\t$id2"
  }
}