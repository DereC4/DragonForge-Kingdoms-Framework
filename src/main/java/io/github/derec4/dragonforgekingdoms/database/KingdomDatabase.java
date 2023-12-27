package io.github.derec4.dragonforgekingdoms.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.derec4.dragonforgekingdoms.kingdom.Kingdom;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class KingdomDatabase {
    private final Cache<String, Kingdom> kingdomCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build();

    public static CompletableFuture<Kingdom> getKingdom(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
           // database operation here

            // once you get the database operation, load from cache
            return null;
        });
    }

    public static void doKingdomOperation(String kingdom, Consumer<Kingdom> operation) {

        getKingdom(kingdom).whenComplete((kingdom2, ex) -> {
           if(ex != null) {
               // do something
               return;
           }

            new BukkitRunnable() {
                @Override
                public void run() {

                    operation.accept(kingdom2);


                }
            };


        });

    }

    static {

    }

}
