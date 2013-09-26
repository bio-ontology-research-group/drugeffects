/* Uses semantic similarity between drug effects to identify drugs with overlapping indications */

float simGIC(Set v1, Set v2) {
  def inter = 0.0
  def un = 0.0
  v1.each { 
    if (v2.contains(it)) {
      inter+=1
    }
    un+=1
  }
  v2.each { un+=1 }
  un-=inter
  if (un == 0.0) {
    return 0.0
  } else {
    return inter/un
  }
}

def inset = new TreeSet()
new File("sider-phenomenet.txt").splitEachLine("\t") { line ->
  if (line[0].startsWith("SIDERMP")) {
    inset.add(line[2])
  }
}
def indications = [:]
new File("indications_raw.tsv").splitEachLine("\t") { line ->
  def sid = line[0]
  def ind = line[1]
  if (sid in inset) {
    if (indications[sid] == null) {
      indications[sid] = new TreeSet()
    }
    indications[sid].add(ind)
  }
}
def positive = [:]
indications.each { sid, ind ->
  indications.each { sid2, ind2 ->
    if (ind.intersect(ind2).size()>0) {
      if (positive[sid] == null) {
	positive[sid] = new TreeSet()
      }
      positive[sid].add(sid2)
    }
  }
}
/*
positive.each { sid, p ->
  if (p && p.size()>1) {
    print "$sid\t"
    p?.each { 
      if (it != sid) {
	print "$it\t"
      }
    }
    println ""
  }
}
*/
def siderorig = [:]
def sidernew = [:]

println "Reading phenotypes"
new File("sider-phenomenet-expanded.txt").splitEachLine("\t") { line ->
  if (line.size()>3) {
    def sid = line[2]
    def mark = line[0]
    if (mark.startsWith("SIDERMP")) {
      sidernew[sid] = new TreeSet()
      line[3..-1].each { sidernew[sid].add(it) }
    } else if (mark.startsWith("SIDERORIG")) {
      siderorig[sid] = new TreeSet()
      line[3..-1].each { siderorig[sid].add(it) }
    }
  }
}

println "Computing similarity"
siderorig.each { sider, phenotypes ->
  def results = []
  siderorig.each { sider2, phenotypes2 ->
    if (sider!=sider2) {
      Expando exp = new Expando()
      exp.sim = simGIC(phenotypes, phenotypes2)
      if (sider2 in positive[sider]) {
	exp.positive = true
      } else {
	exp.positive = false
      }
      results << exp
    }
  }
  results = results.sort { it.sim }.reverse()
  def counter = 0
  def size = results.size()
  results.each { r ->
    if (r.positive) {
      println counter/size+"\t"+r.sim
    }
    counter += 1
  }
}
