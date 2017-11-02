@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')

import groovy.json.*
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON

public class AberOWLClient {
  def API = "http://aber-owl.net/service/api/"

  def getSynonymsFromShort(label) {
    label = label.replace(':', '_')
    def ontology = label.split('_')[0]
    getSynonymsFromShort(label, ontology)
  }

  def getSynonymsFromShort(label, ontology) {
    def http = new HTTPBuilder(API)
    def output = []

    label = 'http://purl.obolibrary.org/obo/' + label // only work for obolibs but ok

    http.request(GET, JSON) {
      uri.path = 'runQuery.groovy'
      uri.query = [
        query: label,
        ontology: ontology,
        limit: 1,
        type: 'equivalent'
      ]

      response.success = { res, json ->
        def entry = json.result[0]
        output += entry.label
        output += entry.synonym
        output += entry.has_related_synonym
        output.removeAll([null])
      }
    }

    return output
  }
}
