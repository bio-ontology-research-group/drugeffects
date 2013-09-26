def infiniteIC = 0 // use this for mismatch between phenotype terms (used in SIDER but not in MP); 0 means to ignore them

def fout = new PrintWriter(new BufferedWriter(new FileWriter("target-similarity.txt")))

float simGIC(Set v1, Set v2, Map icmap) { // v1 and v2 are sets or lists of indices
  def inter = 0.0
  def  un = 0.0
  v1.each { 
    if (v2.contains(it)) {
      inter+=icmap[it]
    }
    un+=icmap[it]
  }
  v2.each { un+=icmap[it] }
  un-=inter
  if (un == 0.0) {
    return 0.0
  } else {
    return inter/un
  }
}

/* start of script */

def simfile = new File("stitch-effects.txt")
def drugs = [:]
def mice = [:]
simfile.splitEachLine("\t") { line ->
  if (line.size()>1) {
    line = line.collect { it.replaceAll("<http://purl.obolibrary.org/obo/","").replaceAll(">","") }
    def id = line[0]
    if (id.startsWith("MGI:")) {
      Set s = new TreeSet()
      line[1..-1].each { s.add(it.replaceAll("_",":")) }
      mice[id] = s
    }
    if (id.startsWith("SIDER:")) {
      Set s = new TreeSet()
      line[1..-1].each { s.add(it.replaceAll("_",":")) }
      drugs[id] = s
    }
  }
}

/* For drug target discovery, we compute only info content for the MGI phenotypes */

def icmap = [:]
def total = 0
def counting = [:]
mice.each { mgi, phenos ->
  phenos.each { mp ->
    if (counting[mp]==null) {
      counting[mp] = 0
    }
    counting[mp]++
  }
  total += 1
}
counting.each { mp, num ->
  def value = counting[mp]
  def ic = -Math.log(value/total)/Math.log(2)
  icmap[mp] = ic
}
/* set unused ICs to max */
drugs.each { mgi, phenos ->
  phenos.each {
    if (icmap[it] == null) {
      icmap[it] = infiniteIC
    }
  }
}

def numdrugs = drugs.size()
total = 0
mice.keySet().each { fout.print(it+"\t") }
fout.println("")
drugs.each { drug, dp ->
  fout.print("$drug\t")
  mice.each { mouse, mp ->
    def val = simGIC(dp, mp, icmap)
    fout.print(val+"\t")
  }
  fout.println("")
  println "$total/$numdrugs finished"
  total += 1
}
fout.flush()
fout.close()
