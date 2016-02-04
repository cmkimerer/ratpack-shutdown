
package com.example.shutdown;

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
            .threads(4)
        ).registryOf(registry -> {
          for (int i = 0; i < 10; i++) {
            registry.add(new DumbService());
          }
        }).handlers(chain ->
            chain.all(ctx -> {
              System.out.println("Shutdown is being called");
              wrapper.stop();
              ctx.render("");
            })
        )
    );

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

    public void stop() throws Exception {
      server.stop();
    }

  }
}