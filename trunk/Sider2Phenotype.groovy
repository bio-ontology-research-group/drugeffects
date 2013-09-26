def umls2phenotype = [:]

def f1 = new File("lexicalmapping.txt")
f1.splitEachLine("\t") { line ->
  def pid = line[0]
  def uid = line[1]
  if (umls2phenotype[uid]==null) {
    umls2phenotype[uid] = new TreeSet()
  }
  umls2phenotype[uid].add(pid)
}
f1 = new File("lexicalmapping-mp.txt")
f1.splitEachLine("\t") { line ->
  def pid = line[0]
  def uid = line[1]
  if (umls2phenotype[uid]==null) {
    umls2phenotype[uid] = new TreeSet()
  }
  umls2phenotype[uid].add(pid)
}
f1 = new File("hpo2umls.txt")
f1.splitEachLine("\t") { line ->
  def pid = line[0]
  def uid = line[1]
  if (umls2phenotype[uid]==null) {
    umls2phenotype[uid] = new TreeSet()
  }
  umls2phenotype[uid].add(pid)
}
f1 = new File("tanya-umls2mp.txt")
f1.splitEachLine(",") { line ->
  def cid = line[0]
  def oid = line[3]?.replaceAll("_",":")
  if (oid?.startsWith("MP") || oid?.startsWith("HP")) {
    if (umls2phenotype[oid]==null) {
      umls2phenotype[oid] = new TreeSet()
    }
    umls2phenotype[oid].add(oid)
  }
}

def meddradrug2effect = [:]
def s = new File("meddra_adverse_effects.tsv")
s.splitEachLine("\t") { line ->
  def drug = line[2]
  def uid = line[6]
  if (meddradrug2effect[drug] == null) {
    meddradrug2effect[drug] = new TreeSet()
  }
  meddradrug2effect[drug].add(uid)
}
def siderdrug2effect = [:]
s = new File("indications_raw.tsv")
s.splitEachLine("\t") { line ->
  def drug = line[0]
  def uid = line[1]
  if (siderdrug2effect[drug] == null) {
    siderdrug2effect[drug] = new TreeSet()
  }
  siderdrug2effect[drug].add(uid)
}

def siderdrug2effectsp = [:]
siderdrug2effect.each { drug, effects ->
  def ss = new TreeSet()
  effects.each {
    if (umls2phenotype[it]!=null) {
      ss.addAll(umls2phenotype[it])
    }
  }
  siderdrug2effectsp[drug] = ss
}

/* replace the names here with IDs to make it URI compliant; write mapping to file */
def mout = new PrintWriter(new FileWriter("id2siderlabel.txt"))
def count = 0
siderdrug2effectsp.each {drug, effects ->
  def dr = "SIDER:$count"
  mout.println("$drug\t$dr")
  if (effects.size()>0) {
    effects.each { println "$dr\t$it" }
  }
  count += 1
}
mout.flush()
mout.close()
