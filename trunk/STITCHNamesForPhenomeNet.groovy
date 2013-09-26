new File("stitch/chemical.aliases.v3.1.tsv").splitEachLine("\t") { line ->
  def id = line[0].replaceAll("CID","")
  def name = line[1]
  println "STITCHORIG:$id\t$name"
  println "MEDLINESTITCHTM:$id\t$name"
}
