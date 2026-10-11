package com.helper.cli.heartbeat;

/** Reports to the server which part of the CLI is alive; drives the green/red lights in the UI. */
public interface IPresenceClient {
    String CLI = "cli";
    String AUTO_SCREEN = "autoscreen";

    /** True when the server accepted the check-in. Never throws. */
    boolean ping(String client);

    /** "Going away" - the UI light turns red immediately. Never throws. */
    void offline(String client);
}
