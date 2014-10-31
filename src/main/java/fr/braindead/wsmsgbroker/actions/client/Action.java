package fr.braindead.wsmsgbroker.actions.client;

/**
 * Created by leiko on 31/10/14.
 */
public enum Action {
    ANSWER {
        @Override
        public String toString() {
            return "answer";
        }
    },
    MESSAGE {
        @Override
        public String toString() {
            return "message";
        }
    },
    REGISTER {
        @Override
        public String toString() {
            return "register";
        }
    },
    REGISTERED {
        @Override
        public String toString() {
            return "registered";
        }
    },
    UNREGISTER {
        @Override
        public String toString() {
            return "unregister";
        }
    },
    UNREGISTERED {
        @Override
        public String toString() {
            return "unregistered";
        }
    },
    SEND {
        @Override
        public String toString() {
            return "send";
        }
    }
}
