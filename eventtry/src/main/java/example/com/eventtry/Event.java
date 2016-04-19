package example.com.eventtry;

/**
 * Created by lll on 2016/4/18.
 */
public class Event {

    public static class EventA {

        private String eventA;

        public EventA(String eventA) {
            this.eventA = eventA;
        }

        public String getEventA() {
            return eventA;
        }
    }

    public static class EventB {

        private String eventB;

        public EventB(String eventB) {
            this.eventB = eventB;
        }

        public String getEventB() {
            return eventB;
        }
    }

    public static class EventC {

        private String eventC;

        public EventC(String eventC) {
            this.eventC = eventC;
        }

        public String getEventC() {
            return eventC;
        }
    }

}
