def d2e = [:]
new File("sider-mp-effects.tsv").splitEachLine("\t") { line ->
  def id = line[-1]
  def mp = line[2]
  if (d2e[id] == null) {
    d2e[id] = new TreeSet()
  }
  d2e[id].add(mp)
}

d2e.keySet().each { drug1 ->
  def fout = new PrintWriter(new BufferedWriter(new FileWriter("enriched-drug-effects/"+drug1+".txt")))
  d2e.each { drug, effects ->
    effects.each { effect ->
      if (drug == drug1) {
	fout.println("$drug\t$effect\t1")
      } else {
	fout.println("$drug\t$effect\t0")
      }
    }
  }
  fout.flush()
  fout.close()
}