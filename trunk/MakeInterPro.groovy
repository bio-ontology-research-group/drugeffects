CUTOFF = 15 // must have more than 15 targets in family


def id = ""
def map = [:]
new File("interpro/ParentChildTreeFile.txt").eachLine { line ->
  if (! line.startsWith("-")) {
    id = line
    id = id.substring(0,id.indexOf(":"))
    map[id] = new TreeSet()
  } else {
    line = line.substring(0,line.indexOf(":"))
    line = line.replaceAll("-","")
    map[id].add(line)
  }
}

def mmap = [:]
new File("interpro/MGI_InterProDomains.rpt").splitEachLine("\t") { line ->
  def i = line[0]
  def mid = line[2]
  if (mid!=null) {
    if (mmap[i] == null) {
      mmap[i] = new TreeSet()
    }
    mmap[i].add(mid)
  }
}

def fmap = [:] // final
map.each { k, v ->
  fmap[k] = new TreeSet()
  if (mmap[k]!=null) {
    fmap[k].addAll(mmap[k])
  }
  v.each { k2 ->
    if (mmap[k2]!=null) {
      fmap[k].addAll(mmap[k2])
    }
  }
}

def mgi2interpro = [:]
fmap.each { k, v ->
  v.each { 
    mgi2interpro[it] = k
  }
}

def outset = new TreeSet()
fmap.each { k, v ->
  if (v.size()>CUTOFF) {
    outset.add(k)
  }
}
def fouts = [:]
outset.each { of ->
  fouts[of] = new PrintWriter(new BufferedWriter(new FileWriter("/tmp/$of")))
}

new File(args[0]).splitEachLine("\t") { line ->
  def sid = line[0]
  def mid = line[1].trim()
  def iid = mgi2interpro[mid]
  if (iid && iid in outset) {
    fouts[iid].println("$sid\t$mid")
  }
}

fouts.each { k, v -> v.flush() ; v.close() ; }