package com.griddynamics.indexer.exceptions.advisers;

import java.util.function.Consumer;

@FunctionalInterface
public interface ConsumerHandler <Target, ExObj extends Exception> {
    void accept(Target target) throws ExObj;

    static <Target> Consumer<Target> consumerHandlerBuilder(
            ConsumerHandler<Target, Exception> handlingConsumer) {
        return obj -> {
            try {
                handlingConsumer.accept(obj);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
