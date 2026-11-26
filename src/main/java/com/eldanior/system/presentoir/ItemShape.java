package com.eldanior.system.presentoir;

import com.eldanior.system.config.EldaniorLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forme d'un objet lue dans son .blockymodel : main(s) qui le tiennent et encombrement (64 unites = 1 bloc). Sert a le centrer et
 * a l'ajuster a la vitrine, sans liste d'objets a tenir a jour. right et left sont faux pour un modele sans os de main (racine
 * Origin, Root...) : il ne peut pas etre attache, il est montre comme modele principal.
 * Reprend la regle de position du rig : un enfant se place par rapport au centre de forme de son parent ; une racine d'attache
 * est ignoree (l'os qui la recoit en tient lieu), une autre racine compte avec sa propre position.
 */
record ItemShape(boolean right, boolean left, float minY, float maxY, float radius) {

    /** Modele illisible : une epee ordinaire. */
    boolean attached() { return right || left; }

    /** Plus grande dimension, largeur de rotation comprise. */
    float size() { return Math.max(maxY - minY, 2 * radius); }

    private static final ItemShape DEFAULT = new ItemShape(true, false, -12, 60, 10);
    private static final Map<String, ItemShape> CACHE = new ConcurrentHashMap<>();

    static ItemShape of(String modelPath) {
        return modelPath == null ? DEFAULT : CACHE.computeIfAbsent(modelPath, ItemShape::read);
    }

    private static ItemShape read(String path) {
        try {
            var asset = CommonAssetRegistry.getByName(path);
            if (asset == null) return DEFAULT;
            JsonObject model = JsonParser.parseString(new String(asset.getBlob().join(), StandardCharsets.UTF_8)).getAsJsonObject();
            boolean right = false, left = false;
            float[] box = {Float.MAX_VALUE, -Float.MAX_VALUE, 0};
            for (JsonElement e : model.getAsJsonArray("nodes")) {
                JsonObject node = e.getAsJsonObject();
                String name = node.has("name") ? node.get("name").getAsString() : "";
                right |= name.equals("R-Attachment");
                left |= name.equals("L-Attachment");
                walk(node, new float[3], new float[]{0, 0, 0, 1}, name.equals("R-Attachment") || name.equals("L-Attachment"), box);
            }
            return box[0] > box[1] ? new ItemShape(right, left, DEFAULT.minY, DEFAULT.maxY, DEFAULT.radius)
                                   : new ItemShape(right, left, box[0], box[1], box[2]);
        } catch (Exception e) {
            EldaniorLogger.error("[Presentoir] lecture du modele " + path, e);
            return DEFAULT;
        }
    }

    /** box : min Y, max Y, rayon horizontal autour de l'axe de l'attache. */
    private static void walk(JsonObject node, float[] parent, float[] parentQ, boolean root, float[] box) {
        JsonObject shape = node.has("shape") ? node.getAsJsonObject("shape") : null;
        float[] pos = root ? new float[3] : add(parent, rotate(parentQ, vec(node, "position")));
        float[] q = root ? new float[]{0, 0, 0, 1} : mul(parentQ, quat(node));
        float[] center = shape != null ? add(pos, rotate(q, vec(shape, "offset"))) : pos;
        if (shape != null && shape.has("settings") && shape.getAsJsonObject("settings").has("size")) {
            float[] size = vec(shape.getAsJsonObject("settings"), "size");
            float[] stretch = shape.has("stretch") ? vec(shape, "stretch") : new float[]{1, 1, 1};
            for (int c = 0; c < 8; c++) {
                float[] corner = {sign(c, 1) * Math.abs(size[0] * stretch[0]) / 2, sign(c, 2) * Math.abs(size[1] * stretch[1]) / 2,
                        sign(c, 4) * Math.abs(size[2] * stretch[2]) / 2};
                float[] p = add(center, rotate(q, corner));
                box[0] = Math.min(box[0], p[1]);
                box[1] = Math.max(box[1], p[1]);
                box[2] = Math.max(box[2], (float) Math.hypot(p[0], p[2]));
            }
        }
        JsonArray children = node.has("children") ? node.getAsJsonArray("children") : null;
        if (children != null) for (JsonElement c : children) walk(c.getAsJsonObject(), center, q, false, box);
    }

    private static float sign(int corner, int bit) { return (corner & bit) != 0 ? 1 : -1; }

    private static float[] vec(JsonObject o, String key) {
        JsonObject v = o.has(key) && o.get(key).isJsonObject() ? o.getAsJsonObject(key) : null;
        return v == null ? new float[3] : new float[]{num(v, "x", 0), num(v, "y", 0), num(v, "z", 0)};
    }

    private static float[] quat(JsonObject node) {
        JsonObject o = node.has("orientation") ? node.getAsJsonObject("orientation") : null;
        return o == null ? new float[]{0, 0, 0, 1} : new float[]{num(o, "x", 0), num(o, "y", 0), num(o, "z", 0), num(o, "w", 1)};
    }

    private static float num(JsonObject o, String key, float fallback) { return o.has(key) ? o.get(key).getAsFloat() : fallback; }

    private static float[] add(float[] a, float[] b) { return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]}; }

    private static float[] rotate(float[] q, float[] v) {
        float tx = 2 * (q[1] * v[2] - q[2] * v[1]), ty = 2 * (q[2] * v[0] - q[0] * v[2]), tz = 2 * (q[0] * v[1] - q[1] * v[0]);
        return new float[]{v[0] + q[3] * tx + q[1] * tz - q[2] * ty, v[1] + q[3] * ty + q[2] * tx - q[0] * tz,
                v[2] + q[3] * tz + q[0] * ty - q[1] * tx};
    }

    private static float[] mul(float[] a, float[] b) {
        return new float[]{a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1], a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
                a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3], a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2]};
    }
}
