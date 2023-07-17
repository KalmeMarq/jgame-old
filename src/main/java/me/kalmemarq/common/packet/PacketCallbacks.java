package me.kalmemarq.common.packet;

import org.jetbrains.annotations.Nullable;

public interface PacketCallbacks {
    static PacketCallbacks always(Runnable runnable) {
        return new PacketCallbacks() {
            @Override
            public void onSuccess() {
                runnable.run();
            }

            @Override
            public void onFailure() {
                runnable.run();
            }
        };
    }

    static PacketCallbacks onSuccess(Runnable runnable) {
        return new PacketCallbacks() {
            @Override
            public void onSuccess() {
                runnable.run();
            }
        };
    }

    static PacketCallbacks onFailure(Runnable runnable) {
        return new PacketCallbacks() {
            @Override
            public void onFailure() {
                runnable.run();
            }
        };
    }

    static PacketCallbacks of(Packet failurePacket) {
        return new PacketCallbacks() {
            @Override
            public @Nullable Packet getFailurePacket() {
                return failurePacket;
            }
        };
    }

    default void onSuccess() {
    }

    default void onFailure() {
    }

    @Nullable
    default Packet getFailurePacket() {
        return null;
    }
}
