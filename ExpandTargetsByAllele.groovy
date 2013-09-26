def map = [:]
new File("mgi/MGI_GenePheno.rpt").splitEachLine("\t") { line ->
  def aid = line[2].split("\\|")
  def gid = line[-1]
  if (map[gid] == null) {
    map[gid] = new TreeSet()
  }
  map[gid].addAll(aid)
}
new File(args[0]).splitEachLine("\t") { line ->
  def sid = line[0]
  def mid = line[1]
  if (map[mid]) {
    map[mid].each {
      println "$sid\t$it"
    }
  } 
}