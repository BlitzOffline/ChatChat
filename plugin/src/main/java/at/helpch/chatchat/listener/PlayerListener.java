package at.helpch.chatchat.listener;

import at.helpch.chatchat.ChatChatPlugin;
import at.helpch.chatchat.api.user.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.stream.Collectors;

public final class PlayerListener implements Listener {

    private final ChatChatPlugin plugin;

    public PlayerListener(@NotNull final ChatChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onJoin(final PlayerJoinEvent event) {
        final var onlinePlayers = "[" + Bukkit.getOnlinePlayers().stream()
            .map(player -> player.getName() + " - " + player.getUniqueId())
            .collect(Collectors.joining(", ")) + "]";
        final var map = new HashMap<String, String>();
        map.put("event", "JOIN");
        map.put("uuid", event.getPlayer().getUniqueId().toString());
        map.put("name", event.getPlayer().getName());
        map.put("onlinePlayers", onlinePlayers);
        map.put("chatUsers", "[" + plugin.usersHolder().users().stream().map(u -> u.uuid().toString()).collect(Collectors.joining(", ")) + "]");
        plugin.logSpecialEvent(map);

        plugin.usersHolder().getUser(event.getPlayer());
    }

    @EventHandler
    private void onLeave(final PlayerQuitEvent event) {
        final var onlinePlayers = "[" + Bukkit.getOnlinePlayers().stream()
            .map(player -> player.getName() + " - " + player.getUniqueId())
            .collect(Collectors.joining(", ")) + "]";
        final var optionalChatUser = plugin.usersHolder().getOptionalUser(event.getPlayer());
        final var map = new HashMap<String, String>();
        map.put("event", "QUIT");
        map.put("uuid", event.getPlayer().getUniqueId().toString());
        map.put("name", event.getPlayer().getName());
        map.put("hasChatUser", String.valueOf(optionalChatUser.isPresent()));
        optionalChatUser.ifPresent(user -> map.put("chatUserType", user.getClass().getSimpleName()));
        map.put("onlinePlayers", onlinePlayers);
        map.put("chatUsers", "[" + plugin.usersHolder().users().stream().map(u -> u.uuid().toString()).collect(Collectors.joining(", ")) + "]");
        plugin.logSpecialEvent(map);

        // find everyone who last messaged the person leaving, and remove their reference
        plugin.usersHolder().users().stream()
                .filter(user -> user instanceof ChatUser)
                .map(user -> (ChatUser) user)
                .filter(user -> user.lastMessagedUser().isPresent())
                .filter(user -> user.lastMessagedUser().get().player().equals(event.getPlayer()))
                .forEach(user -> user.lastMessagedUser(null));

        plugin.usersHolder().removeUser(event.getPlayer());
    }
}
