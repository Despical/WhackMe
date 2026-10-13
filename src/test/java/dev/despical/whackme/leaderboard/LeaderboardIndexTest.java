package dev.despical.whackme.leaderboard;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Despical
 * <p>
 * Created at 13.10.2026
 */
class LeaderboardIndexTest {

    private final LeaderboardIndex index = new LeaderboardIndex("record_score");
    private final UUID first = new UUID(0, 1);
    private final UUID second = new UUID(0, 2);
    private final UUID third = new UUID(0, 3);

    @Test
    void promotesAPlayerPreviouslyOutsideTheVisiblePositions() {
        update(first, "First", 30);
        update(second, "Second", 20);
        update(third, "Third", 10);
        assertEquals(second, index.snapshot(2).getEntryAtPosition(2).uuid());

        update(third, "Third", 40);

        assertEquals(third, index.snapshot(2).getEntryAtPosition(1).uuid());
        assertEquals(first, index.snapshot(2).getEntryAtPosition(2).uuid());
    }

    @Test
    void fillsTheVisiblePositionsWhenALeadingPlayerLosesScore() {
        update(first, "First", 30);
        update(second, "Second", 20);
        update(third, "Third", 10);

        update(first, "First", 5);

        assertEquals(second, index.snapshot(2).getEntryAtPosition(1).uuid());
        assertEquals(third, index.snapshot(2).getEntryAtPosition(2).uuid());
    }

    @Test
    void removesPlayersWhoseScoresBecomeZeroOrNegative() {
        update(first, "First", 30);
        update(second, "Second", 20);

        update(first, "First", 0);
        update(second, "Second", -1);

        assertTrue(index.snapshot(10).sortedEntries().isEmpty());
    }

    @Test
    void keepsTiedPlayersInAStableOrder() {
        update(second, "SameName", 20);
        update(first, "SameName", 20);

        assertEquals(first, index.snapshot(10).getEntryAtPosition(1).uuid());
        assertEquals(second, index.snapshot(10).getEntryAtPosition(2).uuid());
    }

    @Test
    void replacesThePlayersNameWithoutCreatingDuplicateEntries() {
        update(first, "OldName", 10);
        update(first, "NewName", 10);

        assertEquals(1, index.snapshot(10).sortedEntries().size());
        assertEquals("NewName", index.snapshot(10).getEntryAtPosition(1).name());
    }

    @Test
    void leavesPublishedSnapshotsUnchangedAfterAnUpdate() {
        update(first, "First", 10);
        Leaderboard<Integer> published = index.snapshot(10);

        update(first, "First", 20);

        assertEquals(10, published.getEntryAtPosition(1).value());
        assertEquals(20, index.snapshot(10).getEntryAtPosition(1).value());
        assertThrows(UnsupportedOperationException.class, () -> published.sortedEntries().clear());
    }

    @Test
    void retainsHiddenEntriesWhenTheConfiguredSizeIncreases() {
        update(first, "First", 30);
        update(second, "Second", 20);
        update(third, "Third", 10);
        assertEquals(1, index.snapshot(1).sortedEntries().size());

        assertEquals(3, index.snapshot(10).sortedEntries().size());
        assertEquals(third, index.snapshot(10).getEntryAtPosition(3).uuid());
    }

    private void update(UUID uuid, String name, int score) {
        index.update(new LeaderboardEntry<>(uuid, name, score));
    }
}
