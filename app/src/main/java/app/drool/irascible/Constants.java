package app.drool.irascible;

public class Constants {
    public interface CODES {
        int addServerRequestCode = 1001;
        int addServerResponseAdded = 1002;
        int addServerResponseEdited = 1003;
    }

    public interface PREFERENCES {
        interface FILES {
            String savedServers = "irascible.savedServers";
            String generalPreferences = "irascible.generalPrefs";
        }

        interface KEYS {
            String defaultNick = "irascible.defaultNick";
            String savedServers = "irascible.savedServerList";
        }

        interface VALUES {
            String defaultNick = "irscblUser";
        }

    }

    public interface SERVICE {
        interface ACTIONS {
            String startService = "irascible.startservice";
            String stopService = "irascible.stopservice";
            String pauseService = "irascible.pauseservice";
            String resumeService = "irascible.resumeservice";
            String heartbeatRequest = "irascible.heartbeatreq";

            String heartbeatFromService = "irascible.heartbeat";
            String broadcastToActivity = "irascible.toBroadcast";
            String broadcastErrorToActivity = "irascible.toBroadcastErr";
            String broadcastFromActivity = "irascible.fromBroadcast";
            String broadcastToService = broadcastFromActivity;
            String broadcastFromService = broadcastToActivity;
            String broadcastErrorFromService = broadcastErrorToActivity;

        }
    }

    public interface STORAGE {
        interface FILES {
            String sessionLog = "irscbl.sessionLog";
            String fragmentLog = "irscbl.tabLog.";
        }
    }
}
