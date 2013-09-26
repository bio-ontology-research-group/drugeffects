def positivesf = new File("sidertargets.txt")
def matrixf = new File("target-similarity.txt")

def allele2marker = [:]
new File("mgi/MGI_GenePheno.rpt").splitEachLine("\t") { line ->
  def a = line[2]
  def m = line[-1]
  allele2marker[a] = m
}

def positives = [:]
positivesf.splitEachLine("\t") { line ->
  def s = line[0]
  def t = line[1]
  if (positives[s] == null) {
    positives[s] = new TreeSet()
  }
  positives[s].add(t)
}

def quantiles = new TreeSet()

def order = []
def start = true
matrixf.splitEachLine("\t") { line ->
  if (start) {
    start = false
    order = line
  } else {
    def sid = line[0]
    if (positives[sid]) {
      def l = []
      for (int i = 0 ; i < line.size()-1 ; i++) {
	val = new Double(line[i+1])
	def mgi = order[i]
	Expando exp = new Expando()
	exp.val = val
	exp.mgi = mgi
	l << exp
      }
      l = l.sort { it.val }.reverse()
      for (int i = 0 ; i < l.size() ; i++) {
	def test = l[i].mgi
	if (allele2marker[test] != null) {
	  test = allele2marker[test]
	}
	if (test in positives[sid]) {
	  quantiles.add(i/(l.size()-1))
	}
      }
    }
  }
}

def counter = 0
quantiles.each { q ->
  def p = counter/(quantiles.size()-1)
  println "$q\t$p"
  counter += 1
}