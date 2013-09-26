def name2stitch = [:]
new File("stitchphenotypes-names.txt").splitEachLine("\t") { line ->
  def id = line[0]
  def name = line[1].trim().toLowerCase()
  if (name2stitch[name] == null) {
    name2stitch[name] = new TreeSet()
  }
  name2stitch[name].add(id)
}

def names = name2stitch.keySet()
new File("hazards/hazard-db.txt").eachLine { line ->
  def name = line.trim().toLowerCase()
  names.each { 
    if (it.trim().indexOf(name)>-1 && name.length()>=5) {
      name2stitch[it].each { s ->
	println "$name\t$it\t$s"
      }
    }
  }
}

new File("hazards/teratogen-list-rightdiagnosis.txt").eachLine { line ->
  def name = line.trim().toLowerCase()
  names.each { 
    if (it.trim().indexOf(name)>-1 && name.length()>=5) {
      name2stitch[it].each { s ->
	println "$name\t$it\t$s"
      }
    }
  }
}
