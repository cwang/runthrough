@Grab(group='org.mnode.ical4j', module='ical4j', version='1.0.7')
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.property.*
@Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.14') 
import org.cyberneko.html.parsers.SAXParser 
import groovy.util.XmlSlurper

def host = 'http://www.runthrough.co.uk/events-timeline/'
def parser = new SAXParser()
def page = new XmlSlurper(parser).parseText(host.toURL().getText(requestProperties: ['User-Agent': 'Groovy']))

def dateformat = 'MMMM d, yyyy h:mm a'
def stamp = new DtStamp()

def builder = new ContentBuilder()
def calendar = builder.calendar() {
	prodid('-//Groovy/iCal4j 1.0//EN')
	version('2.0')
	page.'**'.findAll { it.name() == 'DIV' && it['@class'] == 'timeline-panel' }.each { e ->

		def day = e.DIV[1].UL[0].LI[0].text()
		day = day.substring(day.lastIndexOf('|') + 1).trim()

		def times = e.DIV[1].UL[0].LI[1].text().split('-')
		def start = Date.parse(dateformat, day + ' ' + times[0].trim().toUpperCase())
		def end = Date.parse(dateformat, day + ' ' + times[1].trim().toUpperCase())

		vevent() {
			uid(e.DIV[0].H3[0].A[0].@href.text())
			dtstamp(stamp)
			dtstart(new DtStart(new net.fortuna.ical4j.model.DateTime(start)))
			dtend(new DtEnd(new net.fortuna.ical4j.model.DateTime(end)))
			url(e.DIV[0].H3[0].A[0].@href.text())
			organizer('RunThrough')
			summary('Race ' + e.DIV[0].H3[0].A[0].text())
			location(e.DIV[1].UL[0].LI[2].text())
		}
	}

}

def f = new File(args[0])
f.text = calendar

//println calendar
