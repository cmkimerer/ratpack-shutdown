
package com.example.shutdown;

import ratpack.exec.Blocking;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

public class Shutdown {

  public static void main(String... args) throws Exception {

    Wrapper wrapper = new Wrapper();

    RatpackServer server = RatpackServer.of(s ->
        s.serverConfig(
            // Note: Shutdown fails when threads is <= 10, sometimes fails when it's just a little
            // higher than 10
            ServerConfig.builder()
                .development(false)
                .threads(4)
        ).handlers(chain ->
            chain.prefix("shutdown", shutdown -> shutdown
                .all(ctx -> {
                  System.out.println("Shutdown is being called");
                  ctx.onClose(outcome -> new Thread(wrapper::stop).start());
                  ctx.render("");
                })).prefix("sleep_blocking", sleep -> sleep
                .all(ctx -> {
                  // sleep 10s
                  Blocking.get(() -> {
                    System.out.println("Request started, sleeping 10s");
                    Thread.sleep(10000);
                    System.out.println("Request finished, returning result now");
                    return true;
                  }).then(x -> ctx.getResponse().send("This request has finished"));
                })).prefix("sleep", sleep -> sleep
                .all(ctx -> {
                  // sleep 10s
                  System.out.println("Request started, sleeping 10s");
                  Thread.sleep(10000);
                  System.out.println("Request finished, returning result now");
                  ctx.getResponse().send("This request has finished");
                }))));

    wrapper.setServer(server);

    server.start();
  }

  private static class Wrapper {
    RatpackServer server;
    public Wrapper() {
    }

    public void setServer(RatpackServer server) {
      this.server = server;
    }

    public void stop() {
      try {
        System.out.println("Shutdown called");
        server.stop();
      } catch (Exception ex) {
        System.out.println("Failed to shutdown with error " + ex.getMessage());
      }
    }
  }
}