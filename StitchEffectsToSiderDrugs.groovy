import java.text.NumberFormat

NumberFormat format = NumberFormat.getInstance()
format.setMinimumIntegerDigits(9)

def stitch2sider = [:]
new File("label_mapping.tsv").splitEachLine("\t") { line ->
  Expando exp = new Expando()
  exp.genericname = line[0]
  exp.brandname = line[1]
  exp.stitchmerged = line[3].replaceAll("-","")
  def stitchid = line[4]?.replaceAll("-","").trim()
  if (stitchid) {
    stitchid = new Integer(stitchid)
    stitchid = format.format(stitchid).replaceAll(",","")
  }
  exp.stitchplain = stitchid
  exp.siderid = line[6]
  if (exp.stitchplain.length()>1) {
    stitch2sider[exp.stitchplain] = exp
  }
}

def id = ""
def id2name = [:]
new File("mammalian_phenotype.obo").eachLine { line ->
  if (line.startsWith("id:")) {
    id = line.substring(3).trim()
  }
  if (line.startsWith("name:")) {
    def name = line.substring(5).trim()
    id2name[id] = name
  }
}

def stitcheffects = [:]
new File("stitch-effects.txt").splitEachLine("\t") { line ->
  def st = line[0].replaceAll("CID","")
  def pheno = line[1]
  def gene = line[2]
  Expando exp = new Expando()
  exp.pheno = pheno
  exp.gene = gene
  if (stitcheffects[st] == null) {
    stitcheffects[st] = new LinkedHashSet()
  }
  stitcheffects[st].add(exp)
}

stitcheffects.each { stitch, effects ->
  if (stitch2sider[stitch]!=null) {
    def exp = stitch2sider[stitch]
    effects.each { exp2 ->
      def effect = exp2.pheno
      def gene = exp2.gene
      println exp.genericname+"\t"+exp.brandname+"\t$effect\t"+id2name[effect]+"\t$stitch\t"+exp.siderid+"\t"+gene
    }
  }
}