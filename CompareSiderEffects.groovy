/* Compares the SIDER drug effects with our predicted drug effects */
/* uses a measure of semantic similarity to determine similarity */

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


def siderorig = [:]
def sidernew = [:]

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

siderorig.each { orig, op ->
  float trueval = -1
  if (orig in sidernew.keySet()) {
    def l = []
    sidernew.each { news, np ->
      def sim = simGIC(op, np)
      l << sim
      if (orig == news) {
	trueval = sim
      }
    }
    def size = l.size()
    def index = 0
    l = l.sort()
    for (int i = 0 ; i < l.size() ; i++ ) {
      if (l[i] == trueval) {
	index = i
      }
    }
    def result = 1-index/size
    println "$index\t$size\t"+result
  }
}
