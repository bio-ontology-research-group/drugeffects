def outset = new TreeSet()
def map = [:]
new File("stitch/chemical.sources.v3.1.tsv").splitEachLine("\t") { line ->
  if (line[0].startsWith("#") == false) {
    def id1 = line[0].replaceAll("CID","STITCHORIG:")
    def id2 = line[1].replaceAll("CID","STITCHORIG:")
    def flag = (line[2] == "ATC")
    def atc = line[3].trim()
    atc = atc.charAt(0)
    if (flag) {
      if (map[id1] == null) {
	map[id1] = new TreeSet()
	map[id1].add(atc)
      }
      if (map[id2] == null) {
	map[id2] = new TreeSet()
	map[id2].add(atc)
      }
      outset.add(atc)
    }
  }
}

def fouts = [:]
outset.each { of ->
  fouts[of] = new PrintWriter(new BufferedWriter(new FileWriter("/tmp/"+args[0]+"-atc-$of")))
}

new File(args[0]).splitEachLine("\t") { line ->
  def sid = line[0]
  def mid = line[1].trim()
  def iid = map[sid]
  if (iid) {
    iid.each { i ->
      fouts[i].println("$sid\t$mid")
    }
  }
}


fouts.each { k, v -> v.flush() ; v.close() ; }
