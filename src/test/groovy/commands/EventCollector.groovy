package commands

class EventCollector {
    def eventList = []

    def methodMissing(String eventName, arguments) {
        eventList << Class.forName("domain.events.$eventName").newInstance(arguments)
    }

    def toList() {
        eventList
    }
}
