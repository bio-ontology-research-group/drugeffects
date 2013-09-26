import java.text.NumberFormat
import java.net.*

PrintWriter fout = new PrintWriter(new FileWriter("siderphenotypes-names.txt"))

NumberFormat format = NumberFormat.getInstance()
format.setMinimumIntegerDigits(9)

new File("label_mapping.tsv").splitEachLine("\t") { line ->
  def stitchid = line[4]?.replaceAll("-","")
  if (stitchid) {
    stitchid = new Integer(stitchid)
    stitchid = format.format(stitchid).replaceAll(",","")
    def name1 = line[0]
    def name2 = line[1]
    if (name1 && name1.length()>1) {
      fout.println("STITCHORIG:$stitchid\t$name1")
      fout.println("STITCHMP:$stitchid\t$name1")
      fout.println("STITCHCOMBINED:$stitchid\t$name1")
    }
    if (name2 && name2.length()>1) {
      fout.println("STITCHORIG:$stitchid\t$name2")
      fout.println("STITCHMP:$stitchid\t$name2")
      fout.println("STITCHCOMBINED:$stitchid\t$name2")
    }
  }
}
fout.flush()
fout.close()

def sider2id = [:]
def sider2name = [:]
def sider2bname = [:]
def count = 1
new File("endresults/sider-mp-effects.tsv").splitEachLine("\t") { line ->
  def siderid = line[-3].trim()
  def id = siderid
  def ontid = line[2]
  def name = ""
  if (line[1].size()>1) {
    name = line[1]
  } else {
    name = line[0]
  }
  println "STITCHMP:$id\t$ontid"
  println "STITCHCOMBINED:$id\t$ontid"
}
fout.flush()
fout.close()

new File("endresults/sider-ontology-representation.txt").splitEachLine("\t") { line ->
  def id = line[1].trim()
  def ontid = line[3]
  println "STITCHORIG:$id\t$ontid"
  println "STITCHCOMBINED:$id\t$ontid"
}


