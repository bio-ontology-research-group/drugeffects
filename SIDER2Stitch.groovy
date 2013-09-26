
def sider2id = [:]
new File("id2siderlabel.txt").splitEachLine("\t") { line ->
  def i = line[0]
  def i2 = line[1]
  sider2id[i] = i2
}

def sider2stitch = [:]
new File("label_mapping.tsv").splitEachLine("\t") { line ->
  def id = line[-1]
  id = sider2id[id]
  def st = line[3].replaceAll("-","")
  sider2stitch[id] = st
  if (id && st) {
    println "$id\t$st"
  }
}

