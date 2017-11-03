def aberowl = new AberOWLClient()
def dbs = [ 'ICD9', 'ICD10', 'SNOMED', 'UMLS', 'OMIM' ]
new File(args[1]).text = "Label\tDOID\t${dbs.join('\t')}\n" + new File(args[0]).text.split('\n').collect { label -> 
  def terms = dbs.collectEntries { [(it): null] }
  def doid
  println "Finding database maps for ${label}"

  def runSearch = { term ->
    [
      aberowl.searchLabels(term, 'DOID').collect { id, oClass ->
        oClass[0].annotation = oClass[0].iri.split('/').last().replace('_', ':') 
        oClass[0]
      }.eachWithIndex { oClass, i ->
        println "${i}: ${oClass.value} (${oClass.annotation})\n\t${oClass.definition}"
      },
      System.console().readLine('Enter choice (number), another search term, or "bad" if you give up: ')
    ]
  }

  def (search, choice) = runSearch(label)
  if(choice != "bad") {
    if(choice.isNumber()) {
      def oClass = aberowl.getClass(search[choice.toInteger()].iri, 'DOID')
      doid = oClass.oboid

      def dbMatch = { cr, db -> cr.split(':')[0].indexOf(db.toLowerCase()) != -1 }
      terms.each { db, x ->
        terms[db] = oClass.database_cross_reference.findAll { dbMatch(it, db) }.join(',')
      }
    } else {
      runSearch(choice)
    }
  }

  "${label}\t${doid}\t" + dbs.collect { terms[it] }.join('\t')
}.join('\n')
