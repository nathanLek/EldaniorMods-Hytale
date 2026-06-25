package com.eldanior.system.titles.nobility.family;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KnightOrderManager {

    private static final Map<String, KnightOrder> orders = new ConcurrentHashMap<>();
    private static final Map<String, List<KnightOrder>> familyOrders = new ConcurrentHashMap<>();
    private static final Map<UUID, String> playerOrderMap = new ConcurrentHashMap<>();
    private static final Map<UUID, KnightOrderRequest> pendingRequests = new ConcurrentHashMap<>();

    // ==================== REQUEST INNER CLASS ====================

    public static class KnightOrderRequest {
        private final UUID requesterUUID;
        private final String requesterName;
        private final String orderName;
        private final String orderMotto;
        private final String familyId;
        private final long requestedAt;

        public KnightOrderRequest(UUID requesterUUID, String requesterName, String orderName, String orderMotto, String familyId) {
            this.requesterUUID = requesterUUID;
            this.requesterName = requesterName;
            this.orderName = orderName;
            this.orderMotto = orderMotto;
            this.familyId = familyId;
            this.requestedAt = System.currentTimeMillis();
        }

        public UUID getRequesterUUID() { return requesterUUID; }
        public String getRequesterName() { return requesterName; }
        public String getOrderName() { return orderName; }
        public String getOrderMotto() { return orderMotto; }
        public String getFamilyId() { return familyId; }
        public long getRequestedAt() { return requestedAt; }
    }

    // ==================== CREATION ====================

    public static KnightOrder createOrder(String name, String motto, String familyId, UUID capitaineUUID, String capitaineName) {
        if (!canCreateOrder(familyId)) return null;

        name = name.trim();
        if (name.length() < 3 || name.length() > 24) return null;

        KnightOrder order = new KnightOrder(name, motto, familyId, capitaineUUID, capitaineName);
        orders.put(order.getId(), order);
        familyOrders.computeIfAbsent(familyId, k -> Collections.synchronizedList(new ArrayList<>())).add(order);
        playerOrderMap.put(capitaineUUID, order.getId());
        return order;
    }

    // ==================== REQUESTS ====================

    public static boolean requestCreation(UUID requesterUUID, String requesterName, String name, String motto, String familyId) {
        KnightOrderRequest request = new KnightOrderRequest(requesterUUID, requesterName, name, motto, familyId);
        pendingRequests.put(requesterUUID, request);
        return true;
    }

    public static KnightOrder approveRequest(UUID patriarchUUID, UUID requesterUUID) {
        KnightOrderRequest request = pendingRequests.get(requesterUUID);
        if (request == null) return null;

        KnightOrder order = createOrder(request.getOrderName(), request.getOrderMotto(), request.getFamilyId(), request.getRequesterUUID(), request.getRequesterName());
        if (order != null) {
            pendingRequests.remove(requesterUUID);
        }
        return order;
    }

    public static boolean rejectRequest(UUID requesterUUID) {
        return pendingRequests.remove(requesterUUID) != null;
    }

    public static List<KnightOrderRequest> getPendingRequestsForFamily(String familyId) {
        List<KnightOrderRequest> result = new ArrayList<>();
        for (KnightOrderRequest request : pendingRequests.values()) {
            if (request.getFamilyId().equals(familyId)) {
                result.add(request);
            }
        }
        return result;
    }

    // ==================== GETTERS ====================

    public static KnightOrder get(String orderId) {
        return orders.get(orderId);
    }

    public static KnightOrder getPlayerOrder(UUID playerUUID) {
        String orderId = playerOrderMap.get(playerUUID);
        if (orderId == null) return null;
        return orders.get(orderId);
    }

    public static List<KnightOrder> getOrdersForFamily(String familyId) {
        List<KnightOrder> list = familyOrders.get(familyId);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

    public static boolean canCreateOrder(String familyId) {
        List<KnightOrder> list = familyOrders.get(familyId);
        return list == null || list.size() < 3;
    }

    public static int getOrderCountForFamily(String familyId) {
        List<KnightOrder> list = familyOrders.get(familyId);
        return list == null ? 0 : list.size();
    }

    public static Collection<KnightOrder> getAll() { return orders.values(); }

    // ==================== MEMBERSHIP ====================

    public static boolean joinOrder(UUID playerUUID, KnightOrder order) {
        if (!order.addMember(playerUUID)) return false;
        playerOrderMap.put(playerUUID, order.getId());
        return true;
    }

    public static boolean leaveOrder(UUID playerUUID) {
        String orderId = playerOrderMap.remove(playerUUID);
        if (orderId == null) return false;

        KnightOrder order = orders.get(orderId);
        if (order == null) return false;

        // Si c'etait le lieutenant, on le retire
        if (order.isLieutenant(playerUUID)) {
            order.clearLieutenant();
        }

        order.removeMember(playerUUID);

        // Si c'etait le capitaine et qu'il n'y a plus de membres, on dissout
        if (order.isCapitaine(playerUUID) && order.getMemberCount() == 0) {
            disbandOrder(orderId);
        }

        return true;
    }

    public static boolean disbandOrder(String orderId) {
        KnightOrder order = orders.remove(orderId);
        if (order == null) return false;

        // Retirer de familyOrders
        List<KnightOrder> list = familyOrders.get(order.getFamilyId());
        if (list != null) {
            list.remove(order);
        }

        // Retirer tous les membres de playerOrderMap
        for (UUID member : new ArrayList<>(order.getMembers())) {
            playerOrderMap.remove(member);
        }

        return true;
    }

    // ==================== LIEUTENANT ====================

    public static boolean setLieutenant(UUID capitaineUUID, UUID targetUUID) {
        KnightOrder order = getPlayerOrder(capitaineUUID);
        if (order == null || !order.isCapitaine(capitaineUUID)) return false;
        if (!order.hasMember(targetUUID)) return false;

        // Recuperer le nom du joueur cible depuis les membres
        order.setLieutenant(targetUUID, targetUUID.toString());
        return true;
    }

    public static boolean removeLieutenant(UUID capitaineUUID) {
        KnightOrder order = getPlayerOrder(capitaineUUID);
        if (order == null || !order.isCapitaine(capitaineUUID)) return false;
        order.clearLieutenant();
        return true;
    }

    // ==================== DISCONNECT ====================

    /** Nettoie les donnees d'un joueur qui se deconnecte. */
    public static void handleDisconnect(UUID playerUUID) {
        // Placeholder pour le moment
    }
}
