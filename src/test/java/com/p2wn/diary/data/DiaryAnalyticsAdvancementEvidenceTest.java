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
    @Test
    @SuppressWarnings("unchecked")
    void resetCutoffExcludesOlderAndSameSecondEventsButKeepsNewerEvidence() throws Exception {
        Plugin plugin=mock(Plugin.class); when(plugin.getDataFolder()).thenReturn(temp.toFile());
        DiaryAnalyticsStore store=new DiaryAnalyticsStore(plugin);
        UUID oldPlayer=UUID.randomUUID(), newPlayer=UUID.randomUUID();
        var field=DiaryAnalyticsStore.class.getDeclaredField("events"); field.setAccessible(true);
        var events=(java.util.List<DiaryAnalyticsEvent>)field.get(store);
        events.add(new DiaryAnalyticsEvent(99,DiaryAnalyticsEventType.INITIAL_ISSUE,oldPlayer,"Old","a","issue"));
        events.add(new DiaryAnalyticsEvent(100,DiaryAnalyticsEventType.DIARY_EDITED,oldPlayer,"Old","a","edited"));
        events.add(new DiaryAnalyticsEvent(101,DiaryAnalyticsEventType.DIARY_EDITED,newPlayer,"New","b","edited"));
        var method=DiaryAnalyticsStore.class.getMethod("advancementEvidenceSummary",long.class);
        var summary=(java.util.Map<UUID,DiaryAdvancementEvidence>)method.invoke(store,100L);
        assertFalse(summary.containsKey(oldPlayer)); assertEquals(1,summary.get(newPlayer).edits());
        assertEquals(3,events.size(),"Filtering advancement migration must not erase the audit log");
    }

    @Test
    void startupAppliesResetBoundaryBeforeReconciliation() throws Exception {
        String source=java.nio.file.Files.readString(Path.of("src/main/java/com/p2wn/diary/DiaryPlugin.java"));
        int reset=source.indexOf("        handleWorldReset();");
        int reconcile=source.indexOf("activeDiaryStore.reconcileAdvancementEvidence(");
        assertTrue(reset>=0 && reset<reconcile);
        assertTrue(source.substring(reconcile,reconcile+250).contains("activeDiaryStore.getAdvancementEvidenceResetAfter()"));
    }

}
