package com.p2wn.diary.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiaryAnalyticsAdvancementEvidenceTest {
    @TempDir Path temp;

    @Test
    void retainedAnalyticsSeedsOnlyProvableDiaryMilestones() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(temp.toFile());
        when(plugin.getConfig()).thenReturn(new YamlConfiguration());
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());

        DiaryAnalyticsStore store = new DiaryAnalyticsStore(plugin);
        UUID player = UUID.randomUUID();
        store.record(DiaryAnalyticsEventType.INITIAL_ISSUE, player, "Player", "diary", "first join");
        store.record(DiaryAnalyticsEventType.DIARY_EDITED, player, "Player", "diary", "edited");
        store.record(DiaryAnalyticsEventType.DIARY_EDITED, player, "Player", "diary", "sign blocked");
        store.record(DiaryAnalyticsEventType.PROTECTED_DESTRUCTION, player, "Player", "diary", "LAVA");
        store.record(DiaryAnalyticsEventType.VOID_RETURN, player, "Player", "diary", "returned to inventory");
        store.record(DiaryAnalyticsEventType.BLOCKED_CONTAINER, player, "Player", "diary", "ENDER_CHEST");
        store.record(DiaryAnalyticsEventType.DIARY_OBTAINED, player, "Player", "diary", "picked up");

        assertEquals(
                new DiaryAdvancementEvidence(true, 1, 1, 1, 1, 1),
                store.advancementEvidenceSummary().get(player));
    }
}
