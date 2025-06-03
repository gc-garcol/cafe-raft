package gc.garcol.caferaft.application;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class CafeRaftApplication {

    public static void main(String[] args) {
        Quarkus.run(args);
    }
} 