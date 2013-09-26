def mousegenes = new TreeSet()
new File("mgi/MGI_GenePheno.rpt").splitEachLine("\t") { line ->
  mousegenes.add(line[-1])
}

def list = []
new File("../phenomeblast/data/c-all-twosided-phenotypes.txt").splitEachLine("\t") { line ->
  list = line
}
list = list.collect {
  def a = it
  a = a.replaceAll("<http://purl.obolibrary.org/obo/","")
  a = a.replaceAll(">","")
  a = a.replaceAll("\n","")
  a.trim()
}

def row = 0
new File("../phenomeblast/data/c-all-twosided.txt").splitEachLine("\t") { line ->
  def l = []
  def b = list[row]
  if (b.indexOf("STITCHORIG")>-1) {
    for (int col = 0 ; col < line.size() ; col++) {
      def d = new Double(line[col])
      def a = list[col]
      if (a in mousegenes) {
	println "$b\t$a\t$d"
      }
    }
  }
  row += 1
}
