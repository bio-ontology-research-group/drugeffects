
def cli = new CliBuilder()
cli.with {
usage: 'Self'
  h longOpt:'help', 'this information'
  a longOpt:'ignore-mode-of-action', 'ignores the mode of action (usually: inhibition)', required:false
  s longOpt:'score', 'minimum STITCH score', args:1, required:false
}
def opt = cli.parse(args)

if( opt.h ) {
    cli.usage()
    return
}

def minscore = 0
if (opt.s) {
  minscore = new Integer(opt.s)
}

if (opt.a == null) {
  opt.a = false
}

/* Selects human and mouse targets from STITCH as positive interactions */

def stitch2sider = [:]
new File("sider2stitch.txt").splitEachLine("\t") { line ->
  def sider = line[0]
  def stitch = "CID"+line[1]
  stitch2sider[stitch] = sider
}

def stitch2entrez = [:] // human stitch to human entrez gene
new File("stitch/entrez_gene_id.vs.string.v9.0.28122012.txt").splitEachLine("\t") { line ->
  def e = line[0]
  def s = line[1]
  stitch2entrez[s] = e
}

def entrez2mgi = [:] // human entrez gene id to MGI marker id
new File("mgi/HMD_HumanSequence.rpt").splitEachLine("\t") { line ->
  def m = line[1]
  def h = line[4]
  entrez2mgi[h] = m
}

def stitch2mgi = [:] // human stitch protein id to MGI marker id
stitch2entrez.each { stitch, entrez ->
  stitch2mgi[stitch] = entrez2mgi[entrez]
}

def mgiens2marker = [:] // maps ENSMUSP..., ENSMUSG... to MGI Marker IDs
new File("mgi/MGI_Coordinate.rpt").splitEachLine("\t") { line ->
  def marker = line[0]
  def e = line[15]
  if (e!=null) {
    mgiens2marker[e] = marker
  }
}

def mouseprotein2gene = [:] // maps mouse proteins to genes (ENSMUSP to ENSMUSG)
new File("stitch/protein.aliases.v9.0.txt").splitEachLine("\t") { line ->
  def g = line[2]
  def p = line[1]
  if (g?.startsWith("ENSMUSG") && p?.startsWith("ENSMUSP")) {
    mouseprotein2gene[p] = g
  }
}

def stitch2inhibitor = [:]
new File("stitch/actions.v3.1.tsv").splitEachLine("\t") { line ->
  if (line[0].indexOf("item")==-1) {
    def s = line[0]
    def i = line[1]
    def score = new Integer(line[5])
    def action = line[3]
    if ((opt.a || action == "inhibition") && i.startsWith("9606") && score > minscore) {
      if (stitch2inhibitor[s] == null) {
	stitch2inhibitor[s] = new TreeSet()
      }
      def mgigene = stitch2mgi[i]
      if (mgigene!=null) {
	stitch2inhibitor[s].add(mgigene)
      }
    }
  }
}

stitch2inhibitor.each { stitch, mgis ->
  mgis.each { mgi ->
    stitch = stitch.replaceAll("CID","")
    println "STITCHORIG:$stitch\t$mgi"
    println "STITCHMP:$stitch\t$mgi"
    println "STITCHCOMBINED:$stitch\t$mgi"
  }
}

/*

def sider2inhibitor = [:]
stitch2inhibitor.each { stitch, genes ->
  def sider = stitch2sider[stitch]
  sider2inhibitor[sider] = genes
}
sider2inhibitor.each { sider, genes ->
  genes.each { g ->
    println "$sider\t$g"
  }
}
*/
