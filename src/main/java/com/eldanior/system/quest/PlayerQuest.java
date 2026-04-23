package com.eldanior.system.quest;

/**
 * Instance d'une quete pour un joueur specifique.
 * Stocke la progression et le statut.
 */
public class PlayerQuest {

    public enum Status { ACCEPTED, ACTIVE, COMPLETED }

    private final String questId;
    private int progress;
    private Status status;
    private long completedTime;

    public PlayerQuest(String questId) {
        this.questId = questId;
        this.progress = 0;
        this.status = Status.ACCEPTED;
    }

    public PlayerQuest(String questId, int progress, Status status, long completedTime) {
        this.questId = questId;
        this.progress = progress;
        this.status = status;
        this.completedTime = completedTime;
    }

    public String getQuestId() { return questId; }
    public int getProgress() { return progress; }
    public Status getStatus() { return status; }

    public void addProgress(int amount) { this.progress += amount; }
    public void setActive() { this.status = Status.ACTIVE; }
    public void setAccepted() { this.status = Status.ACCEPTED; }
    public void setCompleted() { this.status = Status.COMPLETED; this.completedTime = System.currentTimeMillis(); }
    public long getCompletedTime() { return completedTime; }

    public boolean isActive() { return status == Status.ACTIVE; }
    public boolean isCompleted() { return status == Status.COMPLETED; }

    /**
     * Serialize: "questId:progress:status:completedTime"
     */
    public String serialize() {
        return questId + ":" + progress + ":" + status.name() + ":" + completedTime;
    }

    public static PlayerQuest deserialize(String data) {
        String[] parts = data.split(":");
        if (parts.length < 3) return null;
        try {
            Status status = Status.valueOf(parts[2]);
            int progress = Integer.parseInt(parts[1]);
            long completed = parts.length >= 4 ? Long.parseLong(parts[3]) : 0;
            return new PlayerQuest(parts[0], progress, status, completed);
        } catch (Exception e) { return null; }
    }
}
