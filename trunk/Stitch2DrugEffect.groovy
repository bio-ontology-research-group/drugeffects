/* Generates drug effects for STITCH molecules from mouse phenotypes */

def fout = new PrintWriter(new BufferedWriter(new FileWriter("stitch-effects.txt")))

def orthologyf = new File("mgi/HMD_HumanSequence.rpt")
def orthology = [:] // human gene -> mouse gene (using marker ID for mouse, Uniprot ID for human)
orthologyf.splitEachLine("\t") { line ->
  def mg = line[1].trim()
  def hg = line[10].trim()
  orthology[hg] = mg
}

def targets = [:] // drug to targets that are inhibited (using human gene symbol)
def stitchfile = new File("stitch/actions.v3.1.tsv")
def flag = false
stitchfile.splitEachLine("\t") { line ->
  if (!flag) {
    def mode = line[2]
    def first = line[0]
    def second = line[1]
    if (second.indexOf(".")>-1) {
      second = second.substring(second.indexOf(".")+1)
    }
    if (mode == "inhibition") {
      if (targets[first] == null) {
	targets[first] = new TreeSet()
      }
      targets[first].add(second)
    }
  }
}

def values = targets.values()
def idmapping = [:]
new File("idmapping.dat").splitEachLine("\t") { line ->
  def id = line[0]
  def aid = line[2]
  if (id in orthology.keySet()) {
    idmapping[aid] = id
  }
}

def gene2phenotype = [:]
new File("mgi/MGI_GenePheno.rpt").splitEachLine("\t") { line ->
  def gene = line[-1]
  def phenotype = line[4]
  if (gene2phenotype[gene] == null) {
    gene2phenotype[gene] = new TreeSet()
  }
  gene2phenotype[gene].add(phenotype)
}

targets.each { drug, tset ->
  tset.each { target ->
    if (idmapping[target]!=null) {
      if (orthology[idmapping[target]] != null) {
	def mg = orthology[idmapping[target]]
	def phenotypes = gene2phenotype[mg]
	phenotypes?.each { p ->
	  fout.println("$drug\t$p\t$mg")
	}
      }
    }
  }
}

fout.flush()
fout.close()
