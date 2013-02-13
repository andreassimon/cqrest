package commands

class EventCollector {
    def eventList = []

    def methodMissing(String eventName, arguments) {
        eventList << Class.forName(eventName).newInstance(arguments)
    }

    def toList() {
        eventList
    }
}
